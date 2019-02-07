all:
	javac -classpath opencv-343.jar *.java

test:
	$(MAKE) all
	java -classpath .:opencv-343.jar -Djava.library.path=/Users/spiderfencer/opencv_thingy/v1 Main
