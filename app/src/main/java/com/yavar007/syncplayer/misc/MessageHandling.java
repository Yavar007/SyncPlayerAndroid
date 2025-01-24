package com.yavar007.syncplayer.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;


import com.yavar007.syncplayer.MainActivity;
import com.yavar007.syncplayer.models.*;
import com.yavar007.syncplayer.models.CommunicationModels.AcceptMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.AliveMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.MessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.PlayerMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.RequestToJoinMessageModel;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import android.util.Base64;
import java.util.LinkedHashMap;

public class MessageHandling {

    private String secretkey = "b007clientsCnema";

    private String broker = "tcp://broker.emqx.io:1883";
    private String clientId = "ClientC";
    private String jsonString = "";
    private MqttClient client;
    private static String osName;
    private static String deviceName;
    private static String uniqueID;
    private ClientModel clientModel;
    private MainActivity secondForm;
    public MessageHandling(MainActivity secondForm,MqttClient client,ClientModel clientModel) {
        this.client = client;
        this.clientModel=clientModel;
        this.secondForm=secondForm;
    }


    public void sendMessage(String roomID, MessageModel messageModel) {
        String type=messageModel.getType();


//        PlayerMessageModel msgmodel = new PlayerMessageModel(clientModel.getId(),
//                clientModel.getDeviceName(),
//                clientModel.getDeviceOs(),
//                "viewer",
//                message,
//                roomID);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonString = objectMapper.writeValueAsString(messageModel);
            System.out.println(jsonString);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        String encjson = roomID + ":" + encrypt(jsonString, secretkey);
        MqttMessage msg = new MqttMessage(encjson.getBytes());

        msg.setQos(2);
        try {
            client.publish("home/temperature", msg);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Message sent");
    }

    public String encrypt(String strToEncrypt, String secret) {
        try {
            IvParameterSpec iv = generateIv();
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(iv.getIV(), Base64.DEFAULT) + ":" + Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

}
