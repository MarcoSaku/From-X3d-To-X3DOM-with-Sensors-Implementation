/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//PlaneSensor all'interno del nodo Transform
//TouchSensor all'interno di Group
package X3dToX3dom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author MarcoSaku
 */
class ParserX3D {

    private String inputFile = null, outputFile = null;

    private BufferedReader br = null;
    private BufferedReader brSchema = null;

    private BufferedWriter bw = null;
    private BufferedWriter bw2 = null;
    private RandomAccessFile file;
    String line = null;

    private long filePos;
    private int buffPos;
    private ByteBuffer buf;
    private byte[] buf2;
    private byte lastLineBreak = '\n';
    private final byte blankByte = ' ';
    private long oldPointer;
    private final String isActiveString = " onmousedown=\"document.getElementById('timesens').enabled=true;\" onmouseup=\"document.getElementById('timesens').enabled=false;\"";
    private final String isOverString = " onmouseover=\"document.getElementById('timesens').enabled='true';\" onmouseout=\"document.getElementById('timesens').enabled='false';\"";
    private final String touchString = " onclick=\"document.getElementById('timesens').enabled='true';\"";
    private final String dropString = " onmousedown=\"startDragging(this);\" onmouseup=\"stopDragging();\" onmousemove=\"mouseMoved(event);\"";
    private final String rotatexString = " onmousedown=\"startRotating(this);\" onmouseup=\"stopRotatingx();\" onmousemove=\"mouseMoved(event);\"";
    private final String dragScriptString = "<script src=\"Scripts/PlaneScript.js\"></script>\n";
    private final String rotatexScriptString = "<script src=\"Scripts/CylinderScript.js\"></script>\n";
    private final String stringSensorScript = "<script src=\"Scripts/StringSensorScript.js\"></script>\n";

    private String nodeName = null;
    private final String navInfoString = "<navigationInfo id=\"navInfo\" type='\"EXAMINE\" \"ANY\"' typeParams=\"-0.4, 60, 0.5, 1.55\"></navigationInfo>\n";
    private boolean groupFound = false;
    private boolean writeX3dNode = false;
    private boolean writeScriptPlaneSensor = false;
    private boolean navigationInfo = false;
    private boolean writeScriptCylinderSensor = false;
    private boolean writeStringSensor = false;
    private String textStringSensor = " ";
    private GUI gui;

    public void parse(String input, String output, GUI gui) throws IOException {
        this.gui = gui;
        inputFile = input;
        outputFile = output;
        //delete temp file if it exists
        File fileTemp = new File("temp");
        if (fileTemp.exists()) {
            fileTemp.delete();
        }
        //delete output file if it exists
        File fileOut = new File(outputFile);
        if (fileOut.exists()) {
            fileOut.delete();
        }
        file = new RandomAccessFile(fileTemp, "rwd");
        file.seek(0);
        writeX3DOMSchema();
        parseX3DOM();

        //check file temp and write the final xhtml to outputFile
        checkFile();
        file.close();
        fileTemp.deleteOnExit();
        //show log throuhh GUI
        gui.showLog();

    }

    //copy XHTML Schema to temp
    public void writeX3DOMSchema() throws IOException {
        try {
            brSchema = new BufferedReader(new FileReader("Scripts/SchemaX3DOM.html"));
        } catch (FileNotFoundException ex) {
            gui.notFound("SchemaX3DOM.html not Found!");
        }

        line = brSchema.readLine();
        for (int i = 1; i <= 96 && line != null; i++) {
            line += "\n";
            file.write(line.getBytes());
            line = brSchema.readLine();
        }
        brSchema.close();

    }

    //Parse the .x3d file and write to temp
    public void parseX3DOM() throws IOException {
        try {
            //open x3d file
            br = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException ex) {
            gui.notFound(inputFile + " not Found!");
        }
        line = null;

        //parsing x3d file
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            if (line.toUpperCase().contains("<!DOCTYPE X3") || line.toUpperCase().contains("?XML VERSION") || line.toUpperCase().contains("<X3D")) {
                line = "";
            } //Add ID to timesensor
            else if (line.toUpperCase().contains("<TIMESENSOR")) {
                gui.addLog("Timesensor Found!\n");
                setTimeSensor();
            } //TouchSensor with isActive field
            else if (line.contains("fromField=\"isActive\"") || line.contains("fromField='isActive'")) {
                if (line.contains("toField=\"enabled\"") || line.contains("toField='enabled'")) {
                    file.write(line.getBytes());
                    file.write(lastLineBreak);
                    nodeName = "TouchSensor";
                    replaceTouchSensor(isActiveString);
                    nodeName = null;
                    file.seek(file.length());
                }
                //TouchSensor with isOver field
            } else if (line.contains("fromField=\"isOver\"") || line.contains("fromField='isOver'")) {
                if (line.contains("toField=\"set_enabled\"") || line.contains("toField='set_enabled'")) {
                    nodeName = "TouchSensor";
                    file.write(line.getBytes());
                    file.write(lastLineBreak);
                    replaceTouchSensor(isOverString);
                    file.seek(file.length());
                    nodeName = null;
                }
                //TouchSensor with touchTime field
            } else if (line.contains("fromField=\"touchTime\"") || line.contains("fromField='touchTime'")) {
                if (line.contains("toField=\"set_enabled\"") || line.contains("toField='set_enabled'")) {
                    nodeName = "TouchSensor";
                    file.write(line.getBytes());
                    file.write(lastLineBreak);
                    replaceTouchSensor(touchString);
                    file.seek(file.length());
                    nodeName = null;
                }

            }//PlaneSensor with translation_changed field
            else if (line.contains("fromField=\"translation_changed\"") || line.contains("fromField='translation_changed'")) {
                if (line.contains("toField=\"set_translation\"") || line.contains("toField='set_translation'")) {
                    file.write(line.getBytes());
                    file.write(lastLineBreak);
                    long point = file.getFilePointer();
                    nodeName = "PlaneSensor";
                    replacePlaneCylinder(dropString);
                    file.seek(file.length());
                    nodeName = null;
                    writeScriptPlaneSensor = true;
                }
            }//CylinderSensor with rotation_changed field 
            else if (line.contains("fromField=\"rotation_changed\"") || line.contains("fromField='rotation_changed'")) {
                if (line.contains("toField=\"set_rotation\"") || line.contains("toField='set_rotation'")) {
                    file.write(line.getBytes());
                    file.write(lastLineBreak);
                    nodeName = "CylinderSensor";
                    replacePlaneCylinder(rotatexString);
                    file.seek(file.length());
                    nodeName = null;
                    writeScriptCylinderSensor = true;
                }
                //StringSensor with string field
            } else if (line.contains("toField=\"string\"") || line.contains("toField='string'")) {
                nodeName = "StringSensor";
                file.write(line.getBytes());
                file.write(lastLineBreak);
                replaceStringSensor();
                file.seek(file.length());
                nodeName = null;
            } else if (line.toUpperCase().contains("<NAVIGATIONINFO")) {
                navigationInfo = true;
            } //copy line to temp without changes
            else {
                file.write(line.getBytes());
                file.write(lastLineBreak);
            }
        }
        br.close();
        file.write("</div>\n".getBytes());
        file.write("</body>\n".getBytes());
        file.write("</html>\n".getBytes());

    }

    //Add id=timesens to TimeSensor Node
    public void setTimeSensor() throws IOException {
        StringBuilder str1 = new StringBuilder(line);
        str1.insert(line.indexOf("<TimeSensor") + 12, "id='timesens' ");
        str1.insert(str1.length(), "\n");
        file.write(str1.toString().getBytes());

    }

    //It sets the PlaneSensor and CylinderSensor
    public void replacePlaneCylinder(String mouseString) throws IOException {
        String fromNode = null;
        //Extract from Route DEF of fromNode. Check the name between " " or ' '
        Pattern pattern = Pattern.compile("fromNode=\"(.*?)\"");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            fromNode = matcher.group(1);
        }
        if (fromNode == null) {
            pattern = Pattern.compile("fromNode='(.*?)'");
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                fromNode = matcher.group(1);
            }
        }

        String toNode = null;
        pattern = null;
        matcher = null;
        //extract from ROUTE toNode String
        while (toNode == null) {
            pattern = Pattern.compile("toNode=\"(.*?)\"");
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                toNode = matcher.group(1);
            }
            if (toNode == null) {
                pattern = Pattern.compile("toNode='(.*?)'");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    toNode = matcher.group(1);
                }
            }
            if (toNode == null) {
                line = br.readLine();
                file.write(line.getBytes());
                file.write(lastLineBreak);

            }
        }
        String line2 = null;
        boolean exit = false;
        boolean toNodeFound = false;
        String strTemp = null;
        ReadReverse readRev2 = new ReadReverse();
        boolean foundGroup = false;
        groupFound = false;
        file.seek(0);
        ReadReverse readRev = new ReadReverse();
        //read file from start
        while ((line2 = file.readLine()) != null) {
            //if node with FromNode DEF is found 
            if (line2.indexOf("'" + fromNode + "'") != -1) {
                //if PlaneSensor or CylinderSensor node is found
                if (line2.toUpperCase().contains(nodeName.toUpperCase())) {
                    //read in reverse mode out.xhtml through readReverse
                    while (((line = readRev.read(file, file.getFilePointer())).contains("<X3D id='x3dElement'") == false) && groupFound == false) {
                        if (line.contains("ROUTE") == false) {
                            toNodeFound = false;
                            pattern = null;
                            matcher = null;
                            pattern = Pattern.compile("\"(.*?)\"");
                            matcher = pattern.matcher(line);
                            //find toNode between ' ' or " "
                            while (matcher.find()) {
                                strTemp = matcher.group(1);
                                if (strTemp.equals(toNode)) {
                                    toNodeFound = true;
                                }
                            }
                            if (toNodeFound == false) {
                                pattern = Pattern.compile("'(.*?)'");
                                matcher = pattern.matcher(line);
                                while (matcher.find()) {
                                    strTemp = matcher.group(1);
                                    //System.out.println("temp "+strTemp);
                                    if (strTemp.equals(toNode)) {
                                        toNodeFound = true;

                                    }
                                }
                            }
                        }
                        //if DEF="ToNode" found. Usually it is a Transform node
                        if (toNodeFound) {
                            //Message from GUI ("PlaneSensor/CylinderSensor is found!")
                            gui.addLog(nodeName + " Found!\n");
                            //add HTML5 event to node
                            StringBuilder str1 = new StringBuilder(line);
                            str1.insert(line.indexOf(toNode) + toNode.length() + 1, mouseString);
                            str1.insert(str1.length(), "\n");
                            str1.insert(0, "\n");
                            buffPos = readRev.getBufPos();
                            file.seek(buffPos);

                            //replace old Node with blank ''
                            byte[] blankArray = new byte[line.length() + 1];
                            Arrays.fill(blankArray, blankByte);
                            file.write(blankArray);
                            //moveText moves the text to create the space to insert string into out.xhtml
                            moveText(buffPos, str1.toString().getBytes().length, (int) (file.length() - file.getFilePointer()));
                            file.seek(buffPos);
                            //write new line Node with HTML5 event
                            file.write(str1.toString().getBytes());

                            groupFound = true;
                            exit = true;
                            break;

                        }
                        if (exit == true) {
                            break;
                        }

                    }
                }
            }
            if (exit == true) {
                break;
            }
        }

    }

    //TouchSensor with isOver or TouchTime field
    public void replaceTouchSensor(String mouseString) throws IOException {
        String fromNode = null; //String in "fromNode"
        //Check the name between " " or ' '
        Pattern pattern = Pattern.compile("fromNode=\"(.*?)\"");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            fromNode = matcher.group(1);
        }
        if (fromNode == null) {
            pattern = Pattern.compile("fromNode='(.*?)'");
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                fromNode = matcher.group(1);
            }
        }
        String toNode = null;
        pattern = null;
        matcher = null;
        //extract from ROUTE toNode String
        while (toNode == null) {

            pattern = Pattern.compile("toNode=\"(.*?)\"");
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                toNode = matcher.group(1);
            }
            if (toNode == null) {
                pattern = Pattern.compile("toNode='(.*?)'");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    toNode = matcher.group(1);
                }
            }
            if (toNode == null) {
                line = br.readLine();
                file.write(line.getBytes());
                file.write(lastLineBreak);
            }
        }
        String line2 = null;
        pattern = null;
        matcher = null;
        boolean exit = false;
        boolean toNodeFound = false;
        String strTemp = "a";
        ReadReverse readRev2 = new ReadReverse();
        ReadReverse readRev = new ReadReverse();
        file.seek(0);
        //read file from start
        while ((line2 = file.readLine()) != null) {
            pattern = Pattern.compile("'(.*?)'");
            matcher = pattern.matcher(line2);
            while (matcher.find()) {
                strTemp = matcher.group(1);
                if (strTemp.equals(fromNode)) {
                    break;
                }
            }
            //if node with FromNode DEF is found
            if (strTemp.equals(fromNode)) {
                //if we found TouchSensor node (nodeName=TouchSensor)
                if (line2.contains(nodeName)) {
                    //read in reverse mode temp through readReverse
                    while (((line = readRev.read(file, file.getFilePointer())).contains("<X3D id='x3dElement'") == false)) {
                        if (line.toUpperCase().contains("<ROUTE") == false) {
                            //add mouse string into node <Group
                            if (line.toUpperCase().contains("<GROUP")) {
                                //message in GUI ("TouchSensor Found!")
                                gui.addLog(nodeName + " Found!\n");
                                //create new line for <Group
                                StringBuilder str1 = new StringBuilder(line);
                                str1.insert(line.toUpperCase().indexOf("<GROUP") + 6, mouseString);
                                str1.insert(str1.length(), "\n");
                                str1.insert(0, "\n");
                                buffPos = readRev.getBufPos();
                                file.seek(buffPos);
                                //replace old Node with blank ''
                                byte[] blankArray = new byte[line.length() + 1];
                                Arrays.fill(blankArray, blankByte);
                                file.write(blankArray);
                                //moveText moves the text to create the space to insert string into out.xhtml
                                moveText(buffPos, str1.toString().getBytes().length, (int) (file.length() - file.getFilePointer()));
                                file.seek(buffPos);
                                //write new Group node line
                                file.write(str1.toString().getBytes());
                                exit = true;
                                break;
                            }
                        }
                        if (exit == true) {
                            break;
                        }

                    }
                }
            }
            if (exit == true) {
                break;
            }

        }

    }

    //it sets the StringSensor with field string
    public void replaceStringSensor() throws IOException {
        String toNode = null; //String in "fromNode"
        //Check the name between " " or ' '
        Pattern pattern = Pattern.compile("toNode=\"(.*?)\"");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            toNode = matcher.group(1);
        }
        if (toNode == null) {
            pattern = Pattern.compile("toNode='(.*?)'");
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                toNode = matcher.group(1);
            }
        }

        file.seek(0);
        boolean stringSensorFound = false;
        while ((line = file.readLine()) != null) {
            //when we find out the StringSensor node
            if (line.toUpperCase().contains("<STRINGSENSOR")) {
                stringSensorFound = true;
            }
        }
        file.seek(0);
        textStringSensor = "DEF='" + toNode + "'";
        while ((line = file.readLine()) != null) {
            //when we find out the Text node
            if (line.contains(textStringSensor)) {
                //event written in the node < X3D in checkFile()
                writeStringSensor = true;
                //message from the GUI ("StringSensor found!")
                gui.addLog(nodeName + " Found!\n");
            }
        }
    }

    //it moves the text forward to create space to insert string between lines already present
    public void moveText(int buffPos, int offset, int size) throws IOException {
        byte[] bufText = new byte[size];
        file.readFully(bufText);
        String str = new String(bufText);
        file.seek(buffPos + offset);
        file.write(bufText);
    }

    //it checks the temp file created and create the outputFile
    public void checkFile() throws IOException {
        file.seek(0);
        // xhtml outputFile
        bw2 = new BufferedWriter(new FileWriter(outputFile));

        line = null;
        while ((line = file.readLine()) != null) {
            //it checks if we have to add the link for a JS script
            if (line.contains("<head")) {
                line += "\n";
                bw2.write(line);
                if (writeScriptPlaneSensor) {
                    bw2.write(dragScriptString);
                }
                if (writeScriptCylinderSensor) {
                    bw2.write(rotatexScriptString);
                }
                if (writeStringSensor) {
                    bw2.write(stringSensorScript);
                }
                //it checks if we have to set the NavigationInfo node
            } else if (line.toUpperCase().contains("<NAVIGATIONINFO") && (writeScriptPlaneSensor || writeScriptCylinderSensor)) {
                //if we have to set NavigationInfo node and it is present in x3d file, delete it
                while (line.contains("/>") == false && line.toUpperCase().contains("</NAVIGATIONINFO>") == false) {
                    file.readLine();
                }
                bw2.write(navInfoString);

            } //if it is necessary, write navigation info after <Scene> node
            else if ((writeScriptPlaneSensor || writeScriptCylinderSensor) && line.toUpperCase().contains("<SCENE")) {
                line += "\n";
                bw2.write(line);
                bw2.write(navInfoString);

            }//if we need to set a StringSensor we add mouse string into <X3D node 
            else if (writeStringSensor && line.contains("<X3D id='x3dElement'")) {
                StringBuilder str1 = new StringBuilder(line);
                str1.insert(line.indexOf("'x3dElement'") + 12, " keysEnabled='false' onkeypress=\"writeText(event);\"");
                str1.insert(str1.length(), "\n");
                bw2.write(str1.toString());
            }//add the id for JS to StringSensor 
            else if (writeStringSensor && line.contains(textStringSensor)) {
                StringBuilder str1 = new StringBuilder(line);
                str1.insert(line.indexOf(textStringSensor) + textStringSensor.length(), " id='textKeyboard'");
                str1.insert(str1.length(), "\n");
                bw2.write(str1.toString());

                //copy line without changes
            } else {
                line += "\n";
                bw2.write(line);
            }
        }
        bw2.close();
        groupFound = false;
        writeX3dNode = false;
        writeScriptPlaneSensor = false;
        navigationInfo = false;
        writeScriptCylinderSensor = false;
        writeStringSensor = false;
    }

}
