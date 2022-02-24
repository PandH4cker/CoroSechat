#!/bin/sh

C=../../../components

echo "Making Javadocs..."

javadoc -package -version -author \
        -overview ./overview.html \
        -sourcepath ../src:$C/base-core-1.1.1/src:$C/base-opt-1.1.1/src \
        com.gemplus.opencard.terminal.apduio

echo "Done."
