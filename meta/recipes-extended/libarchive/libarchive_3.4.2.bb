SUMMARY = "Support for reading various archive formats"
DESCRIPTION = "C library and command-line tools for reading and writing tar, cpio, zip, ISO, and other archive formats"
HOMEPAGE = "http://www.libarchive.org/"
SECTION = "devel"
LICENSE = "BSD-2-Clause"
LIC_FILES_CHKSUM = "file://COPYING;md5=fe01f5e02b1f0cc934d593a7b0ddceb6"

DEPENDS = "e2fsprogs-native"

PACKAGECONFIG ?= "zlib bz2 xz lzo"

PACKAGECONFIG_append_class-target = "\
	libxml2 \
	${@bb.utils.filter('DISTRO_FEATURES', 'acl xattr', d)} \
"

DEPENDS_BZIP2 = "bzip2-replacement-native"
DEPENDS_BZIP2_class-target = "bzip2"

PACKAGECONFIG[acl] = "--enable-acl,--disable-acl,acl,"
PACKAGECONFIG[xattr] = "--enable-xattr,--disable-xattr,attr,"
PACKAGECONFIG[zlib] = "--with-zlib,--without-zlib,zlib,"
PACKAGECONFIG[bz2] = "--with-bz2lib,--without-bz2lib,${DEPENDS_BZIP2},"
PACKAGECONFIG[xz] = "--with-lzma,--without-lzma,xz,"
PACKAGECONFIG[openssl] = "--with-openssl,--without-openssl,openssl,"
PACKAGECONFIG[libxml2] = "--with-xml2,--without-xml2,libxml2,"
PACKAGECONFIG[expat] = "--with-expat,--without-expat,expat,"
PACKAGECONFIG[lzo] = "--with-lzo2,--without-lzo2,lzo,"
PACKAGECONFIG[nettle] = "--with-nettle,--without-nettle,nettle,"
PACKAGECONFIG[lz4] = "--with-lz4,--without-lz4,lz4,"
PACKAGECONFIG[mbedtls] = "--with-mbedtls,--without-mbedtls,mbedtls,"

EXTRA_OECONF += "--enable-largefile"

SRC_URI = "http://libarchive.org/downloads/libarchive-${PV}.tar.gz \
           file://CVE-2021-36976-1.patch \
           file://CVE-2021-36976-2.patch \
           file://CVE-2021-36976-3.patch \
           file://CVE-2021-23177.patch \
           file://CVE-2021-31566-01.patch \
           file://CVE-2021-31566-02.patch \
           file://CVE-2022-26280.patch \
           file://CVE-2022-36227.patch \
"

SRC_URI[md5sum] = "d953ed6b47694dadf0e6042f8f9ff451"
SRC_URI[sha256sum] = "b60d58d12632ecf1e8fad7316dc82c6b9738a35625746b47ecdcaf4aed176176"

# upstream-wontfix: upstream has documented that reported function is not thread-safe
CVE_CHECK_WHITELIST += "CVE-2023-30571"

inherit autotools update-alternatives pkgconfig

CPPFLAGS += "-I${WORKDIR}/extra-includes"

do_configure[cleandirs] += "${WORKDIR}/extra-includes"
do_configure_prepend() {
	# We just need the headers for some type constants, so no need to
	# build all of e2fsprogs for the target
	cp -R ${STAGING_INCDIR_NATIVE}/ext2fs ${WORKDIR}/extra-includes/
}

ALTERNATIVE_PRIORITY = "80"

PACKAGES =+ "bsdtar"
FILES_bsdtar = "${bindir}/bsdtar"

ALTERNATIVE_bsdtar = "tar"
ALTERNATIVE_LINK_NAME[tar] = "${base_bindir}/tar"
ALTERNATIVE_TARGET[tar] = "${bindir}/bsdtar"

PACKAGES =+ "bsdcpio"
FILES_bsdcpio = "${bindir}/bsdcpio"

ALTERNATIVE_bsdcpio = "cpio"
ALTERNATIVE_LINK_NAME[cpio] = "${base_bindir}/cpio"
ALTERNATIVE_TARGET[cpio] = "${bindir}/bsdcpio"

BBCLASSEXTEND = "native nativesdk"
