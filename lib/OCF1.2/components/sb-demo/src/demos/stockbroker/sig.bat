set sdoldclasspath=%classpath%
set sdpath=%path%

set classpath=..\..\..\Binaries\classes;%classpath%
set path=..\..\..\Binaries\dlls;%path%
appletviewer -J-DOpenCard.autoload=true SignatureDemo.html

set classpath=%sdoldclasspath%
set path=%sdpath%
set sdoldclasspath=
set sdpath=

