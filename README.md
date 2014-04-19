From-X3d-To-X3DOM-with-Sensors-Implementation
=============================================

In X3DOM the sensors TouchSensor, PlaneSensor, SphereSensor, CylinderSensor and
StringSensor aren't implemented! The Non-implementation of Sensors is desired by developers: you have to use HTML5 events!

This software creates an X3DOM scene from a X3D file with the integration of the Sensors: each Sensor is replaced by an HTML5 event that does the same work. <br>
The proposed software takes in input a .x3d file containing a scene, and creates the X3DOM scene (.xhtml file) containing the x3d scene. Moreover it integrates the Sensor in X3DOM through HTML5 events.
In the directory you can find the file Presentation.pdf that is a presentation of the software (in english). You can also find a relation (in italian)

<b>HOW TO USE</b>

1. Go into the directory "Software/Executable"
2. Launch X3dToX3dom.jar
3. Insert the path of .x3d file and .xhtml file (output)

Author: Marco Saviano <br>
email: marco.saviano.89@gmail.com
