FROM debian:jessie
MAINTAINER Matt McCormick "matt.mccormick@kitware.com"

#include "common.debian"
RUN REPO=http://cdn-fastly.deb.debian.org && \
  echo "deb $REPO/debian jessie main\ndeb $REPO/debian jessie-updates main\ndeb $REPO/debian-security jessie/updates main" > /etc/apt/sources.list

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update --yes && apt-get install --yes \
  automake \
  autogen \
  bash \
  build-essential \
  bc \
  bzip2 \
  ca-certificates \
  curl \
  file \
  git \
  gzip \
  zip \
  make \
  ncurses-dev \
  pkg-config \
  libtool \
  python \
  python-pip \
  rsync \
  sed \
  bison \
  flex \
  tar \
  pax \
  vim \
  wget \
  runit \
  xz-utils && \
  apt-get clean --yes

RUN apt-get install openjdk-7-jdk -y
RUN update-alternatives --config java
RUN update-alternatives --config javac

#include "common.docker"
WORKDIR /usr/share
RUN git clone "https://github.com/nojhan/liquidprompt.git" && \
  cd liquidprompt && \
  git checkout v_1.11
COPY imagefiles/.bashrc /root/

WORKDIR /usr/src

COPY imagefiles/install-openssl.sh imagefiles/install-cmake.sh /dockcross/
RUN \
  if [ "$DEFAULT_DOCKCROSS_IMAGE" = "dockcross/manylinux-x86" ]; then \
    /dockcross/install-openssl.sh -32 && \
    /dockcross/install-cmake.sh -32 || exit 1; \
  else \
    /dockcross/install-openssl.sh && \
    /dockcross/install-cmake.sh || exit 1; \
  fi; \
  rm /dockcross/install-openssl.sh /dockcross/install-cmake.sh

COPY imagefiles/cmake.sh /usr/local/bin/cmake
COPY imagefiles/ccmake.sh /usr/local/bin/ccmake

COPY imagefiles/install-ninja.sh /dockcross/
RUN \
  /dockcross/install-ninja.sh \
    -python $([ -e /opt/python/cp35-cp35m/bin/python ] && echo "/opt/python/cp35-cp35m/bin/python" || echo "python") && \
  rm /dockcross/install-ninja.sh

RUN pip install conan

RUN echo "root:root" | chpasswd
WORKDIR /work
ENTRYPOINT ["/dockcross/entrypoint.sh"]

COPY imagefiles/entrypoint.sh imagefiles/dockcross /dockcross/

# Build-time metadata as defined at http://label-schema.org
# Note: To avoid systematic rebuild of dependent images, only
#       name and vcs-url are included.
ARG IMAGE
ARG VCS_URL
LABEL org.label-schema.name=$IMAGE \
      org.label-schema.vcs-url=$VCS_URL \
      org.label-schema.schema-version="1.0"
