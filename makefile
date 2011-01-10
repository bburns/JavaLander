

run: JavaLander.jar
	start view.htm

JavaLander.jar: JavaLander.class
	jar cvf JavaLander.jar *.class

JavaLander.class: JavaLander.java
	javac -deprecation JavaLander.java

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
