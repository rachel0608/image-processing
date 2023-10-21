# image-processing
An image compression system that allows users to apply different convolution filters on given images

How to run it: Use the command java Main with additional flags listed below
	-o <filename> indicates the root name of the output file that your program should write to 
		For example, -o out would write to “out-1.ppm”, “out-2.ppm”, …, “out-8.ppm”
			If the user does not give us a filename using -o, then the program will default
			write to a file called "out.ppm" (and will rewrite "out.ppm" if told to compress)
	-c indicates that you should perform image compression
	-e indicates that you should perform edge detection
	-x for running our own random neighbor filter
	-t indicates that output images should have the quadtree outlined
	
	We assume that only one of -c, -e or -x will be given. However, -t may or may not be present on any filter. 
