LIBMAIN='libs/armeabi-v7a/libmain.so'
#sed -e s/gl_supportMapBuffer/gl_supportAssBuffer/ -i $LIBMAIN
sed -e 's/gl_separatedepthstencil/nosrgb                 /' -i $LIBMAIN
sed -e 's/gl_enablesamplerobjects/disable_srgbtex        /' -i $LIBMAIN
#sed -e s/gl_blitmode/gl_blittits/ -i $LIBMAIN
#sed -e s/gl_dropmips/gl_droptits/ -i $LIBMAIN
#sed -e s/mat_parallaxmap/mat_parallaxsex/ -i $LIBMAIN
#sed -e s/mat_specular/mat_speiular/ -i $LIBMAIN
