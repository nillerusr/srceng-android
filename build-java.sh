ANDROID_JAR=/mnt/data/android.jar
AAPT=/mnt/data/xash3d-android/aapt
DX=/mnt/data/xash3d-android/dx
APKBUILDER=/mnt/data/xash3d-android/apkbuilder
NAME=hl2
mkdir gen
mkdir bin
mkdir bin/classes
$AAPT package -m -J gen/ -M AndroidManifest.xml  -I $ANDROID_JAR
$JAVA_HOME/bin/javac -d bin/classes -s bin/classes -cp $ANDROID_JAR  src/org/libsdl/app/*.java src/com/nvidia/*.java src/com/valvesoftware/ValveActivity.java
$DX --dex --output=bin/classes.dex bin/classes/
$AAPT package -f -M AndroidManifest.xml -I $ANDROID_JAR -F bin/$NAME.apk.unaligned
$APKBUILDER bin/$NAME.apk -u -nf libs/ -rj libs -f bin/classes.dex -z bin/$NAME.apk.unaligned
java -jar /mnt/app/apktool/signapk.jar /mnt/app/apktool/testkey.x509.pem /mnt/app/apktool/testkey.pk8 bin/$NAME.apk bin/$NAME-signed.apk
