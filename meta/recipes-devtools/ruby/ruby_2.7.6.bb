require ruby.inc

DEPENDS_append_libc-musl = " libucontext"

SRC_URI += " \
           file://remove_has_include_macros.patch \
           file://run-ptest \
           file://0001-Modify-shebang-of-libexec-y2racc-and-libexec-racc2y.patch \
           file://0001-template-Makefile.in-do-not-write-host-cross-cc-item.patch \
           file://CVE-2023-28756.patch \
           file://CVE-2021-33621.patch \
           "

SRC_URI[md5sum] = "f972fb0cce662966bec10d5c5f32d042"
SRC_URI[sha256sum] = "e7203b0cc09442ed2c08936d483f8ac140ec1c72e37bb5c401646b7866cb5d10"

# CVE-2021-28966 is Windows specific and not affects Linux OS
# https://security-tracker.debian.org/tracker/CVE-2021-28966
CVE_CHECK_WHITELIST += "CVE-2021-28966"

PACKAGECONFIG ??= ""
PACKAGECONFIG += "${@bb.utils.filter('DISTRO_FEATURES', 'ipv6', d)}"

PACKAGECONFIG[valgrind] = "--with-valgrind=yes, --with-valgrind=no, valgrind"
PACKAGECONFIG[gmp] = "--with-gmp=yes, --with-gmp=no, gmp"
PACKAGECONFIG[ipv6] = "--enable-ipv6, --disable-ipv6,"

EXTRA_OECONF = "\
    --disable-versioned-paths \
    --disable-rpath \
    --disable-dtrace \
    --enable-shared \
    --enable-load-relative \
    --with-pkg-config=pkg-config \
"

EXTRA_OECONF_append_libc-musl = "\
    LIBS='-lucontext' \
    ac_cv_func_isnan=yes \
    ac_cv_func_isinf=yes \
"

do_install() {
    oe_runmake 'DESTDIR=${D}' install
}

do_install_append_class-target () {
    # Find out rbconfig.rb from .installed.list
    rbconfig_rb=`grep rbconfig.rb ${B}/.installed.list`
    # Remove build host directories
    sed -i -e 's:--sysroot=${STAGING_DIR_TARGET}::g' \
           -e s:'--with-libtool-sysroot=${STAGING_DIR_TARGET}'::g \
           -e 's|${DEBUG_PREFIX_MAP}||g' \
           -e 's:${HOSTTOOLS_DIR}/::g' \
           -e 's:${RECIPE_SYSROOT_NATIVE}::g' \
           -e 's:${RECIPE_SYSROOT}::g' \
           -e 's:${BASE_WORKDIR}/${MULTIMACH_TARGET_SYS}::g' \
        ${D}$rbconfig_rb
}

do_install_ptest () {
    cp -rf ${S}/test ${D}${PTEST_PATH}/

    install -D ${S}/tool/test/runner.rb ${D}${PTEST_PATH}/tool/test/runner.rb
    cp -r ${S}/tool/lib ${D}${PTEST_PATH}/tool/
    mkdir -p ${D}${PTEST_PATH}/lib
    cp -r ${S}/lib/did_you_mean ${S}/lib/rdoc ${D}${PTEST_PATH}/lib

    # install test-binaries
    find $(find ./.ext -path '*/-test-') -name '*.so' -print0 \
        | tar --no-recursion --null -T - --no-same-owner --preserve-permissions -cf - \
        | tar -C ${D}${libdir}/ruby/${SHRT_VER}.0/ --no-same-owner --preserve-permissions --strip-components=2 -xf -
    # adjust path to not assume build directory layout
    sed -e 's|File.expand_path(.*\.\./bin/erb[^)]*|File.expand_path("${bindir}/erb"|g' \
        -i ${D}${PTEST_PATH}/test/erb/test_erb_command.rb

    cp -r ${S}/include ${D}/${libdir}/ruby/
    test_case_rb=`grep rubygems/test_case.rb ${B}/.installed.list`
    sed -i -e 's:../../../test/:../../../ptest/test/:g' ${D}/$test_case_rb
}

PACKAGES =+ "${PN}-ri-docs ${PN}-rdoc"

SUMMARY_${PN}-ri-docs = "ri (Ruby Interactive) documentation for the Ruby standard library"
RDEPENDS_${PN}-ri-docs = "${PN}"
FILES_${PN}-ri-docs += "${datadir}/ri"

SUMMARY_${PN}-rdoc = "RDoc documentation generator from Ruby source"
RDEPENDS_${PN}-rdoc = "${PN}"
FILES_${PN}-rdoc += "${libdir}/ruby/*/rdoc ${bindir}/rdoc"

FILES_${PN} += "${datadir}/rubygems"

FILES_${PN}-ptest_append_class-target = "\
    ${libdir}/ruby/include \
    ${libdir}/ruby/${SHRT_VER}.0/*/-test- \
"

BBCLASSEXTEND = "native"
