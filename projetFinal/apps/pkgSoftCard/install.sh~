#!/bin/bash
#
# This script installs the applets on the card and retrieve the user PIN and PUK.
#
# by Emmanuel Mocquet

# Looking for GPShell

echo -n "Verifying executability of GPShell: "
test -x /usr/bin/gpshell
if [ $? -ne 0 ]; then
    echo "GPShell is not installed... Exiting."
    exit 1
fi
echo "Done."

# Install applets
echo -n "Installing applets: "
gpshell installTunnel.txt &>/dev/null
if [ $? -ne 0 ]; then
    echo "Error. Exiting. (Please check if \"pcscd\" is running."
    exit 1
fi
echo "Done."

# Get PIN
echo -n "Getting PIN: "
pin=$(./AdminTool.jar PIN)
if [ $? -ne 0 ]; then
    echo "Error. Exiting."
    exit 1
fi
echo $pin

# Get PUK
echo -n "Getting PUK: "
puk=$(./AdminTool.jar PUK)
if [ $? -ne 0 ]; then
    echo "Errror. Exiting."
    exit 1
fi
echo $puk
