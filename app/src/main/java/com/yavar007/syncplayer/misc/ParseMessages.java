package com.yavar007.syncplayer.misc;


import android.content.Context;
import android.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yavar007.syncplayer.R;
import com.yavar007.syncplayer.models.CommunicationModels.AcceptMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.AliveMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.ClientJoinedMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.MessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.PlayerMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.RejectMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.RequestToJoinMessageModel;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class ParseMessages {

    public MessageModel parseMessage(String message, Context context) {

        String decryptedMessage=decrypt(message,context);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // First, read the JSON as a generic JsonNode to inspect the type field
            JsonNode jsonNode = objectMapper.readTree(decryptedMessage);
            String type = jsonNode.get("type").asText();
            // Deserialize to the correct class based on the type field
            return switch (type) {
                case "play" -> objectMapper.treeToValue(jsonNode, PlayerMessageModel.class);
                case "rej" -> objectMapper.treeToValue(jsonNode, RejectMessageModel.class);
                case "req" -> objectMapper.treeToValue(jsonNode, RequestToJoinMessageModel.class);
                case "acc" -> objectMapper.treeToValue(jsonNode, AcceptMessageModel.class);
                case "alv" -> objectMapper.treeToValue(jsonNode, AliveMessageModel.class);
                case "clj" -> objectMapper.treeToValue(jsonNode, ClientJoinedMessageModel.class);
                default -> objectMapper.treeToValue(jsonNode, MessageModel.class);
                // Default case for unknown types
            };
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    private static String decrypt(String strToDecrypt,Context context) {
        String secretkey =  context.getString(R.string.secretKey);
        try {
            String[] parts = strToDecrypt.split(":");
            IvParameterSpec iv = new IvParameterSpec(Base64.decode(parts[0], Base64.NO_WRAP));
            SecretKeySpec secretKey = new SecretKeySpec(secretkey.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] original = cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }

}
