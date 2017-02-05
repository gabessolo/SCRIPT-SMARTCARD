#!/bin/bash

if [ "$(id -u)" != "0" ]; then
    echo "This script must be run as root" 1>&2
    exit 1
fi

yum  update > /dev/null
yum install -y libudev-dev libcurl4-openssl-dev pcscd pcsc-tools > /dev/null

if [ $? -ne 0 ]
then
    echo "An error occured while installing packages."
    exit 1;
fi

( cd pcsc-lite-1.8.8 && ./configure --prefix=/usr/local && make && make install ) 
if [ $? -ne 0 ]
then
    echo "An error occured: pcsc-lite-1.8.8"
    exit 1;
fi


( cd globalplatform-6.0.0 && ./configure --prefix=/usr/local && make && make install )
if [ $? -ne 0 ]
then
    echo "An error occured: globalplatform-6.0.0"
    exit 1;
fi

( cd gpshell-1.4.4 && ./configure --prefix=/usr/local && make && make install ) 
if [ $? -ne 0 ]
then
    echo "An error occured: gpshell-1.4.4"
    exit 1;
fi

( cd gppcscconnectionplugin-1.1.0 && ./configure --prefix=/usr/local && make && make install)
if [ $? -ne 0 ]
then
    echo "An error occured: gppcscconnectionplugin-1.1.0"
    exit 1;
fi


( cd syncapi_lnx-1.5.0 && ./install -prefix /usr/local ) 
if [ $? -ne 0 ]
then
    echo "An error occured: syncapi_lnx-1.5.0"
    exit 1;
fi

pcscd
if [ $? -ne 0 ]
then
    echo "An error occured: pcscd"
    exit 1;
fi
echo "PCSC started"

echo "Installation complete"
