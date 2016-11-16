
# JavaLander build file

run: JavaLander.jar
	start index.htm

JavaLander.jar: JavaLander.class Base.class Clouds.class Flame.class Land.class Moon.class Point2D.class Segment.class ShapeX.class Ship.class Sprite.class Stars.class Transform.class View.class World.class
	jar cvf JavaLander.jar *.class

JavaLander.class: JavaLander.java
	javac -deprecation -Xlint:unchecked JavaLander.java

clean:
	rm -f *.class


#~ classes\JavaLander.class: JavaLander.java
  #~ rmdir /s /q classes
  #~ mkdir classes
  #~ javac -deprecation -d classes JavaLander.java
  # make a jar file
  #~ cd classes
  #~ jar cvf JavaLander.jar *.class
  # put it back in the parent folder
  #~ move JavaLander.jar ..
  #~ cd ..
