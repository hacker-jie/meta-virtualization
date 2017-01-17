HOMEPAGE = "https://github.com/jfrazelle/riddler"
SUMMARY = "Convert `docker inspect` to opencontainers (OCI compatible) runc spec."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=20ce4c6a4f32d6ee4a68e3a7506db3f1"
DEPENDS = "go-cross"

SRC_URI = "git://github.com/jfrazelle/riddler;branch=master"
SRCREV = "23befa0b232877b5b502b828e24161d801bd67f6"
PV = "0.1.0+git${SRCPV}"

S = "${WORKDIR}/git"

inherit go-osarchmap

# This disables seccomp and apparmor, which are on by default in the
# go package. 
EXTRA_OEMAKE="BUILDTAGS=''"

do_compile() {
	export GOARCH="${TARGET_GOARCH}"

	# Setup vendor directory so that it can be used in GOPATH.
	#
	# Go looks in a src directory under any directory in GOPATH but riddler
	# uses 'vendor' instead of 'vendor/src'. We can fix this with a symlink.
	#
	# We also need to link in the ipallocator directory as that is not under
	# a src directory.
	ln -sfn . "${S}/vendor/src"
	mkdir -p "${S}/vendor/src/github.com/jessfraz/riddler"
	ln -sfn "${S}/parse" "${S}/vendor/src/github.com/jessfraz/riddler/parse"
	export GOPATH="${S}/vendor"

	# Pass the needed cflags/ldflags so that cgo
	# can find the needed headers files and libraries
	export CGO_ENABLED="1"
	export CFLAGS=""
	export LDFLAGS=""
	export CGO_CFLAGS="${BUILDSDK_CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export CGO_LDFLAGS="${BUILDSDK_LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"

	oe_runmake static
}

do_install() {
	install -d ${D}/${sbindir}
	install ${S}/riddler ${D}/${sbindir}/riddler
}
