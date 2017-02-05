#!/bin/bash 

if [ `ìd -u` -ne 0 ] ; then
    echo "You need to be root"
    exit 1
fi

if [ $SUDO_USER == "root" ] ; then
    echo "Do a 'sudo' from your user account"
    exit 2
fi

sudo su $USOD_USER -c "mkdir /home/$SUDO_USER/.ssn" && chown $SUDO_USER /home/$SUDO_USER/.ssn

(cd installSmartCard && ./install.sh)
