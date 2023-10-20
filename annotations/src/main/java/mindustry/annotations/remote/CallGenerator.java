package mindustry.annotations.remote;

import arc.files.Fi;
import arc.struct.*;
import arc.util.io.*;
import com.squareup.javapoet.*;
import mindustry.annotations.Annotations.*;
import mindustry.annotations.*;
import mindustry.annotations.misc.JS.ClassBuilder;
import mindustry.annotations.misc.JS.CodeBlock;
import mindustry.annotations.misc.JS.ObjectBuilder;
import mindustry.annotations.util.*;
import mindustry.annotations.util.TypeIOResolver.*;

import javax.lang.model.element.*;
import java.io.*;
import java.util.HashMap;
import java.util.Set;

import static mindustry.annotations.BaseProcessor.*;

/** Generates code for writing remote invoke packets on the client and server. */
public class CallGenerator{

    static String[] bridgeTable = {"boolean", "char", "byte"};
    static HashMap<String, String> methodTable = new HashMap<>();
    static HashMap<String, String> methodTableString = new HashMap<>();
    static int build = 146;//坏

    static {
        methodTable.put(".*mindustry\\.ui\\..*", "");
    }

    static {
        methodTableString.put("mindustry.core.NetClient", "n");
        methodTableString.put("mindustry.core.NetServer", "n");
        methodTableString.put("mindustry.world.Tile", "Tile");
        methodTableString.put("mindustry.input.InputHandler", "InputHandler");
    }

    /** Generates all classes in this list. */
    public static void generate(ClassSerializer serializer, Seq<MethodEntry> methods) throws IOException{
        //create builder
        TypeSpec.Builder callBuilder = TypeSpec.classBuilder(RemoteProcess.callLocation).addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder register = MethodSpec.methodBuilder("registerPackets")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        CodeBlock code = new CodeBlock(0);
        int[] packetID = {4};
        code.add("//CODEGEN from squi2rel (github.com/squi2rel/Mindustry-CN-ARC) build" + build);
        code.add("const Packet=require(\"./Packet\")");
        code.add("const TypeIO=require(\"./TypeIO\")");
        code.add("const crc32=require(\"crc-32\")");
        code.add("const Packets=new Map()");
        code.add("class StreamBegin extends Packet{\n" +
                "    _id=0;\n" +
                "    static #lastid=0;\n" +
                "    total;\n" +
                "    type;\n" +
                "    constructor(){\n" +
                "        super();\n" +
                "        this.id=StreamBegin.#lastid++\n" +
                "    }\n" +
                "    write(buf){\n" +
                "        buf.putInt(this.id);\n" +
                "        buf.putInt(this.total);\n" +
                "        buf.put(type)\n" +
                "    }\n" +
                "    read(buf){\n" +
                "        this.id=buf.getInt();\n" +
                "        this.total=buf.getInt();\n" +
                "        this.type=buf.get()\n" +
                "    }\n" +
                "}\n" +
                "Packets.set(0,StreamBegin);\n" +
                "class StreamChunk extends Packet{\n" +
                "    _id=1;\n" +
                "    id;\n" +
                "    data;\n" +
                "    write(buf){\n" +
                "        buf.putInt(this.id);\n" +
                "        buf.putShort(this.data.length);\n" +
                "        buffer.put(this.data)\n" +
                "    }\n" +
                "    read(buf){\n" +
                "        this.id=buf.getInt();\n" +
                "        this.data=buf.get(buf.getShort())\n" +
                "    }\n" +
                "}\n" +
                "Packets.set(1,StreamChunk);\n" +
                "class WorldStream extends Packet{\n" +
                "    _id=2;\n" +
                "    stream;\n" +
                "    handleClient(nc){\n" +
                "        if(nc.game){\n" +
                "            nc.loadWorld(this)\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "Packets.set(2,WorldStream);\n" +
                "class ConnectPacket extends Packet{\n" +
                "    _id=3;\n" +
                "    name;\n" +
                "    usid;\n" +
                "    uuid;\n" +
                "    write(buf){\n" +
                "        buf.putInt(" + build + ");\n" +
                "        TypeIO.writeString(buf,\"official\");\n" +
                "        TypeIO.writeString(buf,this.name);\n" +
                "        TypeIO.writeString(buf,\"Mars\");\n" +
                "        TypeIO.writeString(buf,this.usid);\n" +
                "        let uuidbuf=Buffer.from(this.uuid,\"base64\");\n" +
                "        buf.put(uuidbuf);\n" +
                "        buf.putLong(crc32.buf(uuidbuf));\n" +
                "        buf.put(0);\n" +
                "        buf.put([0xff,0xa1,0x08,0xff]);\n" +
                "        buf.put(0)\n" +
                "    }\n" +
                "}\n" +
                "Packets.set(3,ConnectPacket)");

        //go through each method entry in this class
        for(MethodEntry ent : methods){
            //builder for the packet type
            TypeSpec.Builder packet = TypeSpec.classBuilder(ent.packetClassName)
            .addModifiers(Modifier.PUBLIC);

            ClassBuilder cb = code.newClass(ent.packetClassName, "Packet");
            cb.addVariable("_id", String.valueOf(packetID[0]));

            //temporary data to deserialize later
            packet.addField(FieldSpec.builder(byte[].class, "DATA", Modifier.PRIVATE).initializer("NODATA").build());

            packet.superclass(tname("mindustry.net.Packet"));

            //return the correct priority
            if(ent.priority != PacketPriority.normal){
                packet.addMethod(MethodSpec.methodBuilder("getPriority")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class).returns(int.class).addStatement("return $L", ent.priority.ordinal())
                .build());

                cb.addMethod("getPriority", new String[]{}).add("return " + ent.priority.ordinal());
            }

            //implement read & write methods
            makeWriter(packet, ent, serializer, cb);
            makeReader(packet, ent, serializer, cb);

            //generate handlers
            if(ent.where.isClient){
                packet.addMethod(writeHandleMethod(ent, false, cb));
            }

            if(ent.where.isServer){
                packet.addMethod(writeHandleMethod(ent, true, cb));
            }

            //register packet
            register.addStatement("mindustry.net.Net.registerPacket($L.$L::new)", packageName, ent.packetClassName);

            //add fields to the type
            Seq<Svar> params = ent.element.params();
            for(int i = 0; i < params.size; i++){
                if(!ent.where.isServer && i == 0){
                    continue;
                }

                Svar param = params.get(i);
                packet.addField(param.tname(), param.name(), Modifier.PUBLIC);
            }

            //write the 'send event to all players' variant: always happens for clients, but only happens if 'all' is enabled on the server method
            if(ent.where.isClient || ent.target.isAll){
                writeCallMethod(callBuilder, ent, true, false, cb);
            }

            //write the 'send event to one player' variant, which is only applicable on the server
            if(ent.where.isServer && ent.target.isOne){
                writeCallMethod(callBuilder, ent, false, false, cb);
            }

            //write the forwarded method version
            if(ent.where.isServer && ent.forward){
                writeCallMethod(callBuilder, ent, true, true, cb);
            }

            //write the completed packet class
            JavaFile.builder(packageName, packet.build()).build().writeTo(BaseProcessor.filer);

            code.add("Packets.set(" + packetID[0]++ + "," + ent.packetClassName + ")");
        }

        callBuilder.addMethod(register.build());

        ObjectBuilder ob = code.add("module.exports=", cb -> cb.noSemicolon = true).newObject();
        ob.set("StreamBegin").set("StreamChunk").set("WorldStream").set("ConnectPacket");

        for(MethodEntry ent : methods){
            ob.set(ent.packetClassName);
        }

        ob.set("get", new CodeBlock("n=>Packets.get(n)"));

        //build and write resulting class
        TypeSpec spec = callBuilder.build();
        JavaFile.builder(packageName, spec).build().writeTo(BaseProcessor.filer);

        new Fi("Packets.js").writeString(code.build());
    }

    private static void makeWriter(TypeSpec.Builder typespec, MethodEntry ent, ClassSerializer serializer, ClassBuilder js){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
            .addParameter(Writes.class, "WRITE")
            .addModifiers(Modifier.PUBLIC).addAnnotation(Override.class);
        Seq<Svar> params = ent.element.params();

        CodeBlock jsBuilder = js.addMethod("write", new String[]{"buf"});

        for(int i = 0; i < params.size; i++){
            //first argument is skipped as it is always the player caller
            if(!ent.where.isServer && i == 0){
                continue;
            }

            Svar var = params.get(i);

            //name of parameter
            String varName = var.name();
            //name of parameter type
            String typeName = var.mirror().toString();
            //special case: method can be called from anywhere to anywhere
            //thus, only write the player when the SERVER is writing data, since the client is the only one who reads the player anyway
            boolean writePlayerSkipCheck = ent.where == Loc.both && i == 0;

            if(writePlayerSkipCheck){ //write begin check
                builder.beginControlFlow("if(mindustry.Vars.net.server() || mindustry.Vars.replayController.writing)");
            }

            if(BaseProcessor.isPrimitive(typeName)){ //check if it's a primitive, and if so write it
                builder.addStatement("WRITE.$L($L)", typeName.equals("boolean") ? "bool" : typeName.charAt(0) + "", varName);
                jsBuilder.add("buf.put" + (contains(bridgeTable, typeName) ? "" : typeName.substring(0,1).toUpperCase() + typeName.substring(1)) + "(this." + varName + ")");
            }else{
                //else, try and find a serializer
                String ser = serializer.getNetWriter(typeName.replace("mindustry.gen.", ""), SerializerResolver.locate(ent.element.e, var.mirror(), true));

                if(ser == null){ //make sure a serializer exists!
                    BaseProcessor.err("No method to write class type: '" + typeName + "'", var);
                }

                //add statement for writing it
                builder.addStatement(ser + "(WRITE, " + varName + ")");
                jsBuilder.add(ser.replace("mindustry.io.", "") + "(buf,this." + varName + ")");
            }

            if(writePlayerSkipCheck){ //write end check
                builder.endControlFlow();
            }
        }

        typespec.addMethod(builder.build());
    }

    private static void makeReader(TypeSpec.Builder typespec, MethodEntry ent, ClassSerializer serializer, ClassBuilder js){
        MethodSpec.Builder readbuilder = MethodSpec.methodBuilder("read")
            .addParameter(Reads.class, "READ")
            .addParameter(int.class, "LENGTH")
            .addModifiers(Modifier.PUBLIC).addAnnotation(Override.class);

        //read only into temporary data buffer
        readbuilder.addStatement("DATA = READ.b(LENGTH)");

        typespec.addMethod(readbuilder.build());

        MethodSpec.Builder builder = MethodSpec.methodBuilder("handled")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class);

        CodeBlock jsBuilder = js.addMethod("read", new String[]{"buf"});

        //make sure data is present, begin reading it if so
        builder.addStatement("BAIS.setBytes(DATA)");

        Seq<Svar> params = ent.element.params();

        //go through each parameter
        for(int i = 0; i < params.size; i++){
            Svar var = params.get(i);

            //first argument is skipped as it is always the player caller
            if(!ent.where.isServer && i == 0){
                continue;
            }

            //special case: method can be called from anywhere to anywhere
            //thus, only read the player when the CLIENT is receiving data, since the client is the only one who cares about the player anyway
            boolean writePlayerSkipCheck = ent.where == Loc.both && i == 0;

            if(writePlayerSkipCheck){ //write begin check
                builder.beginControlFlow("if(mindustry.Vars.net.client())");
            }

            //full type name of parameter
            String typeName = var.mirror().toString();
            //name of parameter
            String varName = var.name();
            //capitalized version of type name for reading primitives
            String pname = typeName.equals("boolean") ? "bool" : typeName.charAt(0) + "";

            js.addVariable(varName);
            //write primitives automatically
            if(BaseProcessor.isPrimitive(typeName)){
                builder.addStatement("$L = READ.$L()", varName, pname);
                jsBuilder.add("this." + varName + "=buf.get" + (contains(bridgeTable, typeName) ? "" : typeName.substring(0,1).toUpperCase() + typeName.substring(1)) + "()");
            }else{
                //else, try and find a serializer
                String ser = serializer.readers.get(typeName.replace("mindustry.gen.", ""), SerializerResolver.locate(ent.element.e, var.mirror(), false));

                if(ser == null){ //make sure a serializer exists!
                    BaseProcessor.err("No read method to read class type '" + typeName + "' in method " + ent.targetMethod + "; " + serializer.readers, var);
                }

                //add statement for reading it
                builder.addStatement("$L = $L(READ)", varName, ser);
                jsBuilder.add("this." + varName + "=" + ser.replace("mindustry.io.", "") + "(buf)");
            }

            if(writePlayerSkipCheck){ //write end check
                builder.endControlFlow();
            }
        }

        typespec.addMethod(builder.build());
    }

    /** Creates a specific variant for a method entry. */
    private static void writeCallMethod(TypeSpec.Builder classBuilder, MethodEntry ent, boolean toAll, boolean forwarded, ClassBuilder js){
        Smethod elem = ent.element;
        Seq<Svar> params = elem.params();

        //create builder
        MethodSpec.Builder method = MethodSpec.methodBuilder(elem.name() + (forwarded ? "__forward" : "")) //add except suffix when forwarding
        .addModifiers(Modifier.STATIC)
        .returns(void.class);

        //forwarded methods aren't intended for use, and are not public
        if(!forwarded){
            method.addModifiers(Modifier.PUBLIC);
        }

        //validate client methods to make sure
        if(ent.where.isClient){
            if(params.isEmpty()){
                BaseProcessor.err("Client invoke methods must have a first parameter of type Player", elem);
                return;
            }

            if(!params.get(0).mirror().toString().contains("Player")){
                BaseProcessor.err("Client invoke methods should have a first parameter of type Player", elem);
                return;
            }
        }

        //if toAll is false, it's a 'send to one player' variant, so add the player as a parameter
        if(!toAll){
            method.addParameter(ClassName.bestGuess("mindustry.net.NetConnection"), "playerConnection");
        }

        //add sender to ignore
        if(forwarded){
            method.addParameter(ClassName.bestGuess("mindustry.net.NetConnection"), "exceptConnection");
        }

        //call local method if applicable, shouldn't happen when forwarding method as that already happens by default
        if(!forwarded && ent.local != Loc.none){
            //add in local checks
            if(ent.local != Loc.both){
                method.beginControlFlow("if(" + getCheckString(ent.local) + " || !mindustry.Vars.net.active())");
            }

            //concatenate parameters
            int index = 0;
            StringBuilder results = new StringBuilder();
            for(Svar var : params){
                //special case: calling local-only methods uses the local player
                if(index == 0 && ent.where == Loc.client){
                    results.append("mindustry.Vars.player");
                }else{
                    results.append(var.name());
                }
                if(index != params.size - 1) results.append(", ");
                index++;
            }

            //add the statement to call it
            method.addStatement("$N." + elem.name() + "(" + results + ")",
            ((TypeElement)elem.up()).getQualifiedName().toString());

            if(ent.local != Loc.both){
                method.endControlFlow();
            }
        }

        //start control flow to check if it's actually client/server so no netcode is called
        method.beginControlFlow("if(" + getCheckString(ent.where) + ")");

        //add statement to create packet from pool
        method.addStatement("$1T packet = new $1T()", tname("mindustry.gen." + ent.packetClassName));

        method.addTypeVariables(Seq.with(elem.e.getTypeParameters()).map(BaseProcessor::getTVN));

        for(int i = 0; i < params.size; i++){
            //first argument is skipped as it is always the player caller
            if((!ent.where.isServer) && i == 0){
                continue;
            }

            Svar var = params.get(i);

            method.addParameter(var.tname(), var.name());

            //name of parameter
            String varName = var.name();
            //special case: method can be called from anywhere to anywhere
            //thus, only write the player when the SERVER is writing data, since the client is the only one who reads it
            boolean writePlayerSkipCheck = ent.where == Loc.both && i == 0;

            if(writePlayerSkipCheck){ //write begin check
                method.beginControlFlow("if(mindustry.Vars.net.server())");
            }

            method.addStatement("packet.$L = $L", varName, varName);

            if(writePlayerSkipCheck){ //write end check
                method.endControlFlow();
            }
        }

        String sendString;

        if(forwarded){ //forward packet
            if(!ent.local.isClient){ //if the client doesn't get it called locally, forward it back after validation
                sendString = "mindustry.Vars.net.send(";
            }else{
                sendString = "mindustry.Vars.net.sendExcept(exceptConnection, ";
            }
        }else if(toAll){ //send to all players / to server
            sendString = "mindustry.Vars.net.send(";
        }else{ //send to specific client from server
            sendString = "playerConnection.send(";
        }

        //send the actual packet
        method.addStatement(sendString + "packet, " + (!ent.unreliable) + ")");


        //end check for server/client
        method.endControlFlow();

        //add method to class, finally
        classBuilder.addMethod(method.build());
    }

    private static String getCheckString(Loc loc){
        return
            loc.isClient && loc.isServer ? "mindustry.Vars.net.server() || mindustry.Vars.net.client()" :
            loc.isClient ? "mindustry.Vars.net.client()" :
            loc.isServer ? "mindustry.Vars.net.server()" : "false";
    }

    /** Generates handleServer / handleClient methods. */
    public static MethodSpec writeHandleMethod(MethodEntry ent, boolean isClient, ClassBuilder js){

        //create main method builder
        MethodSpec.Builder builder = MethodSpec.methodBuilder(isClient ? "handleClient" : "handleServer")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(Override.class)
        .returns(void.class);

        CodeBlock jsBuilder = js.addMethod(isClient ? "handleClient" : "handleServer", new String[]{"n"});

        Smethod elem = ent.element;
        Seq<Svar> params = elem.params();

        if(!isClient){
            //add player parameter
            builder.addParameter(ClassName.get("mindustry.net", "NetConnection"), "con");

            //skip if player is invalid
            builder.beginControlFlow("if(con.player == null || con.kicked)");
            builder.addStatement("return");
            builder.endControlFlow();

            //make sure to use the actual player who sent the packet
            builder.addStatement("mindustry.gen.Player player = con.player");
        }

        //execute the relevant method before the forward
        //if it throws a ValidateException, the method won't be forwarded
        builder.addStatement("$N." + elem.name() + "(" + params.toString(", ", s -> s.name()) + ")", ((TypeElement)elem.up()).getQualifiedName().toString());
        jsBuilder.add(fullReplace(((TypeElement)elem.up()).getQualifiedName().toString() + "." + elem.name() + "(" + params.toString(", ", s -> s.name()) + ")"));

        //call forwarded method, don't forward on the client reader
        if(ent.forward && ent.where.isServer && !isClient){
            //call forwarded method
            builder.addStatement("$L.$L.$L__forward(con, $L)", packageName, ent.className, elem.name(), params.toString(", ", s -> s.name()));
        }

        return builder.build();
    }
    public static boolean contains(String[] arr, String name) {
        for(String i : arr) {
            if(name.equals(i)) return true;
        }
        return false;
    }
    public static String fullReplace(String str) {
        Set<String> list = methodTable.keySet();
        String[] result = {str};
        for (String k : list) {
            result[0] = result[0].replaceAll(k, methodTable.get(k));
        }
        Set<String> list2 = methodTableString.keySet();
        for (String k : list2) {
            result[0] = result[0].replace(k, methodTableString.get(k));
        }
        return result[0];
    }
}
