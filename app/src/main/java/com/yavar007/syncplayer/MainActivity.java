package com.yavar007.syncplayer;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.navigation.NavigationView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.yavar007.syncplayer.database.RoomDatabaseHelper;
import com.yavar007.syncplayer.misc.ParseMessages;
import com.yavar007.syncplayer.misc.RoomIdGenerator;
import com.yavar007.syncplayer.misc.UsersListAdapter;
import com.yavar007.syncplayer.models.ClientModel;
import com.yavar007.syncplayer.models.CommunicationModels.AcceptMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.AliveMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.PlayerMessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.MessageModel;
import com.yavar007.syncplayer.models.CommunicationModels.RequestToJoinMessageModel;
import com.yavar007.syncplayer.models.UsersInRoomModel;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {
    private ExoPlayer player;
    private PlayerView playerView;
    private EditText urlEditText;
    private Button playButton;
    private TextView tv_RoomID;
    private boolean isFullscreen = false;
    private String selectedAudio = "-1";
    private String subTitle = "-1";
    private boolean isServer=true;
    private String username;
    private MqttClient mqttClient;
    private ClientModel client;
    private String movieLInk="";
    private String jsonString = "";
    private String osName = "";
    private String deviceName= "";
    private String clientId = "";
    private int movieTimeDelay =500;
    private String roomID="";
    private boolean interruptedWhile =false;
    private UsersListAdapter usersListAdapter;
    private boolean subDisabled =false;
    private boolean audioDisabled =false;
    private RoomDatabaseHelper roomDatabaseHelper;
    private TextView usersCountLabel;
    private TextView usersCountInRoom;
    private ScheduledExecutorService specChecker;
    private boolean isClientConnected=true;
    private Thread checkEverySecond=null;
    private EditText et_Delay;
    private TextView tv_Delayhint;
    private LinearLayout delayHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Bundle bundle=getIntent().getExtras();
        if (bundle!=null){
            isServer=bundle.getBoolean("isServer");
            username=bundle.getString("username");
        }
        osName = "Android "+ Build.VERSION.RELEASE;
        deviceName= Build.MODEL;
        String combinedInfo = osName + "-" + deviceName;
        clientId = UUID.nameUUIDFromBytes(combinedInfo.getBytes()).toString();
        client=new ClientModel(clientId,deviceName,osName,username);
        roomDatabaseHelper=new RoomDatabaseHelper(this);
        roomDatabaseHelper.deleteAllData();

        if (roomDatabaseHelper.selectAllData().isEmpty()){
            roomDatabaseHelper.insertData(new UsersInRoomModel(clientId
                    ,username
                    ,osName
                    ,isServer?"server":"client"
                    ,true,
                    System.currentTimeMillis()));

        }
        //Region Do Not Change The Order
        InitializeUI();
        SetupPlayerAndView();
        PlayerEvents();
        CheckPlayerStatusEverySecond();
        InitializeMQTTClient();
        MessageReceiver();
        //EndRegion
        specChecker = Executors.newScheduledThreadPool(1);
        goPortrait();

    }
    @OptIn(markerClass = UnstableApi.class)
    private void InitializeUI(){
        NavigationView navigationView = findViewById(R.id.navigation_view);
        //usersCountLabel = navigationView.getHeaderView(0).findViewById(R.id.usersCountLabel);
        usersCountLabel = findViewById(R.id.usersCountLabel);
        usersCountInRoom = findViewById(R.id.usersCountInRoom);
        usersCountLabel.setText(R.string.usersInRoomCountLabel);
        usersListAdapter=new UsersListAdapter(this,roomDatabaseHelper.selectAllData());
        RecyclerView recyclerView = findViewById(R.id.recycler_view_sidebar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(usersListAdapter);
        updateUserCount(roomDatabaseHelper.selectAllData().size());
        tv_RoomID=findViewById(R.id.tv_RoomID);
        playerView = findViewById(R.id.playerView);
        urlEditText = findViewById(R.id.et_urlfield);
        et_Delay=findViewById(R.id.et_Delay);
        tv_Delayhint=findViewById(R.id.tv_DelayHint);
        delayHolder=findViewById(R.id.delayHolder);
        playButton = findViewById(R.id.btn_Play);

        ImageButton playPauseButton=playerView.findViewById(R.id.exo_play_pause);
        ImageButton playingSettings=playerView.findViewById(androidx.media3.ui.R.id.exo_settings);
        ImageButton subtitleButton = playerView.findViewById(androidx.media3.ui.R.id.exo_subtitle);
        DefaultTimeBar playerSeekbar=playerView.findViewById(androidx.media3.ui.R.id.exo_progress);
        if (Locale.getDefault().getLanguage().equals("fa")) {
            Typeface typeface = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typeface = getResources().getFont(R.font.yekan);
            }
            playButton.setTypeface(typeface);
            urlEditText.setTypeface(typeface);
            tv_RoomID.setTypeface(typeface);
            tv_Delayhint.setTypeface(typeface);
        }
        if (!isServer){
            playButton.setText(R.string.btnJoinPLay);
            playPauseButton.setVisibility(View.GONE);
            playingSettings.setVisibility(View.GONE);
            subtitleButton.setVisibility(View.GONE);
            playerSeekbar.setVisibility(View.GONE);
            playerView.setShowSubtitleButton(false);
            urlEditText.setHint(R.string.etRoomIDField);
            et_Delay.setText(String.valueOf(movieTimeDelay));
        }
        else{
            roomID= new RoomIdGenerator().generateRoomId();
            tv_RoomID.setText(roomID);
            urlEditText.setHint(R.string.etUrlFieldHint);
            playerView.setShowSubtitleButton(true);
            delayHolder.setVisibility(View.INVISIBLE);
            delayHolder.getLayoutParams().height=0;
        }
        playButton.setOnClickListener(_ -> {
            if (mqttClient!=null ){
                if (mqttClient.isConnected()){
                    if (isServer){
                        loadVideo();
                        if (!movieLInk.isEmpty()){
                            if (movieLInk.contains(".mkv") || movieLInk.contains(".mp4")){
                                play();
                                if (!checkEverySecond.isAlive()){
                                    checkEverySecond.start();
                                }
                            }
                            else {
                                if (player.isPlaying()){
                                    player.stop();
                                }
                                Toast.makeText(MainActivity.this, R.string.errMovieLinkVerify, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else{
                        roomID=urlEditText.getText().toString();
                        if (!roomID.isEmpty()){
                            tv_RoomID.setText(roomID);
                            sendMessage(roomID,
                                    new RequestToJoinMessageModel(clientId
                                            , "req", deviceName, osName, "client", roomID, username));
                        }
                        else {
                            Toast.makeText(MainActivity.this, R.string.errRoomIDEmpty, Toast.LENGTH_SHORT).show();
                        }
                    }
                    startUserCheck();
                }

            }
        });
        tv_RoomID.setOnClickListener(_ -> {
            String ctext=tv_RoomID.getText().toString();
            if (!ctext.isEmpty()){
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("",ctext ); // Empty label
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, String.format(getString(R.string.toastRoomIDCopied), ctext), Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.errRoomIDNA, Toast.LENGTH_SHORT).show();
            }
        });
        et_Delay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>2){
                    int delay=Integer.parseInt(charSequence.toString());
                    if (delay>=500){
                        movieTimeDelay=delay;
                    }
                    else{
                        Toast.makeText(MainActivity.this, getString(R.string.delayLowValue), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
    @OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
    private void SetupPlayerAndView() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setShowSubtitleButton(true);
        playerView.setShowNextButton(false);
        playerView.setShowFastForwardButton(false);
        playerView.setShowPreviousButton(false);
        playerView.setShowRewindButton(false);
        playerView.setShowShuffleButton(false);
        playerView.setShowVrButton(false);
        playerView.setShowPlayButtonIfPlaybackIsSuppressed(false);
        playerView.setOnClickListener(_ -> {
            if (playButton.getVisibility()==View.VISIBLE){
                playerView.hideController();
                playButton.setVisibility(View.GONE);
                urlEditText.setVisibility(View.GONE);
                tv_RoomID.setVisibility(View.GONE);
            }
            else {
                playerView.showController();
                playButton.setVisibility(View.VISIBLE);
                urlEditText.setVisibility(View.VISIBLE);
                tv_RoomID.setVisibility(View.VISIBLE);
            }
        });
        playerView.setFullscreenButtonClickListener(_ -> {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == 1) {
                goLandscape();
            } else if (orientation == 2) {
                goPortrait();
            }
        });
    }
    @OptIn(markerClass = UnstableApi.class)
    private void PlayerEvents(){
        player.addListener(new Player.Listener() {
            @Override
            public void onEvents(@NonNull Player player, @NonNull Player.Events events) {

                Player.Listener.super.onEvents(player, events);
            }
            @Override
            public void onTracksChanged(@NonNull Tracks tracks) {
                if (tracks.containsType(C.TRACK_TYPE_AUDIO)) {
                    ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
                    for (Tracks.Group trackGroup : trackGroups) {
                        if (trackGroup.isSelected()) {
                            if (trackGroup.getTrackFormat(0).sampleMimeType.contains("audio")) {
                                selectedAudio = trackGroup.getMediaTrackGroup().id;

                            }
                            if (trackGroup.getTrackFormat(0).codecs != null) {
                                if (trackGroup.getTrackFormat(0).codecs.contains("subrip")) {
                                    subTitle = trackGroup.getMediaTrackGroup().id;
                                }
                            }

                        }
                        else{
                            if (trackGroup.getTrackFormat(0).codecs != null) {
                                if (trackGroup.getTrackFormat(0).codecs.contains("subrip")) {
                                    //trackGroup.getMediaTrackGroup().id

                                    subTitle = trackGroup.getMediaTrackGroup().id;
                                }
                            }
                        }
                    }
                }


                Player.Listener.super.onTracksChanged(tracks);
            }
            @Override
            public void onMediaMetadataChanged(@NonNull MediaMetadata mediaMetadata) {
                Player.Listener.super.onMediaMetadataChanged(mediaMetadata);
            }
            @Override
            public void onTrackSelectionParametersChanged(@NonNull TrackSelectionParameters parameters) {
                ImmutableSetMultimap<TrackGroup, TrackSelectionOverride> ob = parameters.overrides.asMultimap();

                if (ob.isEmpty()) {
                    subTitle = "-1";
                    selectedAudio="-1";
                } else {
                    for (TrackGroup trackGroup : ob.keys().asList()) {
                        if (trackGroup != null){
                            if (trackGroup.getFormat(0).sampleMimeType.contains("audio")) {
                                selectedAudio = trackGroup.id;
                            }
                            if (trackGroup.getFormat(0).codecs != null) {
                                if (trackGroup.getFormat(0).codecs.contains("subrip")) {
                                    subTitle = trackGroup.id;
                                }
                            }
                        }
                    }
                }

                Player.Listener.super.onTrackSelectionParametersChanged(parameters);

            }
            @Override
            public void onPlaybackStateChanged(int playbackState) {

                Player.Listener.super.onPlaybackStateChanged(playbackState);
            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {

                if (!mqttClient.isConnected()){
                    if (!isServer){
                        Toast.makeText(MainActivity.this, R.string.errDisconnected, Toast.LENGTH_SHORT).show();
                        stop();
                        player.release();
                    }
                }
                Player.Listener.super.onIsPlayingChanged(isPlaying);
            }
        });
    }
    private void CheckPlayerStatusEverySecond(){
        checkEverySecond=new Thread(()->{
            while (!interruptedWhile){
                try {
                    Thread.sleep(3000);
                    runOnUiThread(() -> {
                        if (mqttClient.isConnected()){
                            if (player.isPlaying()) {
                                int sub = Integer.parseInt(subTitle);
                                int audio = Integer.parseInt(selectedAudio);
                                String msg = player.getCurrentPosition() + ":" + sub + ":" + audio;
                                PlayerMessageModel play = new PlayerMessageModel(clientId,
                                        "play", deviceName, osName,
                                        "server",username, msg, roomID);
                                sendMessage(roomID, play);
                                Log.v(TAG, "Time Sent: " + msg);
                            } else {
                                String msg = "paused";
                                PlayerMessageModel play = new PlayerMessageModel(clientId,
                                        "play", deviceName, osName,
                                        "server",username, msg, roomID);
                                sendMessage(roomID, play);
                                Log.v(TAG, "Time Sent: " + msg);
                            }
                        }
                        else {
                            if (player.isPlaying()){
                                stop();
                                player.release();
                            }
                            Toast.makeText(MainActivity.this,R.string.errDisconnected, Toast.LENGTH_SHORT).show();
                        }

                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private void InitializeMQTTClient(){
        try {
            String broker = "tcp://broker.emqx.io:1883";
            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);
            if (mqttClient.isConnected()){
                isClientConnected=true;
                System.out.println("Connected");
                Toast.makeText(this, R.string.connectedToServer, Toast.LENGTH_SHORT).show();
                MqttMessage msg = new MqttMessage(("User"+clientId+" joined").getBytes());
                msg.setQos(2);
                mqttClient.publish("syncplayer/rooms", msg);
            }
            else{
                System.out.println("Not Connected");
                isClientConnected=false;
                Toast.makeText(this, R.string.errDisconnected, Toast.LENGTH_SHORT).show();
            }

        } catch (MqttException e) {
            System.out.println(e.getMessage());
        }
    }
    private void MessageReceiver(){
        try {
            mqttClient.subscribe("syncplayer/rooms", (s, mqttMessage) -> {
                String remsg = new String(mqttMessage.getPayload());
                if (s.equals("syncplayer/rooms")){
                    if (remsg.startsWith(roomID)) {
                        remsg = remsg.split(":")[1]+":"+remsg.split(":")[2];
                        MessageModel message=new ParseMessages().parseMessage(remsg,MainActivity.this);
                        if (!message.getId().equals(client.getId())){
                            if (isServer){
                                if (message.getType().equals("req")){
                                    RequestToJoinMessageModel req=(RequestToJoinMessageModel) message;
                                    if (req.getRoomId().equals(roomID)){
                                        String answer=req.getId();
                                        String username1 =req.getUserName();
                                        String deviceOS=req.getDeviceOs();
                                        String clientRole=req.getClientRole();
                                        if (roomDatabaseHelper.canAddUser()){
                                            sendMessage(roomID,
                                                    new AcceptMessageModel(client.getId(),
                                                            "acc",
                                                            client.getDeviceName()
                                                            ,client.getDeviceOs(),
                                                            "server",client.getUserName(),
                                                            answer,movieLInk,roomID));
                                            UsersInRoomModel spectatorModel= new UsersInRoomModel(answer,
                                                    username1,deviceOS,clientRole,true,
                                                    System.currentTimeMillis());
                                            addNewSpectator(spectatorModel);
                                        }
                                    }
                                }else if (message.getType().equals("alv")){
                                    AliveMessageModel alive=(AliveMessageModel) message;
                                    if (alive.getRoomId().equals(roomID)){
                                        editSpectator(
                                                new UsersInRoomModel(alive.getId(),
                                                        alive.getUserName(),
                                                        alive.getDeviceOs(),
                                                        alive.getClientRole(),
                                                        true,
                                                        System.currentTimeMillis()));
                                    }
                                }
                            }
                            else {
                                if (message.getType().equals("acc")){
                                    AcceptMessageModel acc=(AcceptMessageModel)message;
                                    if (acc.getMessage().equals(client.getId())){
                                        setMovieLink(acc.getMovieLink());
                                        UsersInRoomModel server=new UsersInRoomModel(acc.getId(),
                                                acc.getUserName(),acc.getDeviceOs(),"server",true,
                                                System.currentTimeMillis());
                                        addNewSpectator(server);
//                                            ClientJoinedMessageModel clj=new ClientJoinedMessageModel(clientId,
//                                                    "clj",
//                                                    deviceName,osName,"client",username,acc.getRoomId());
//                                            sendMessage(acc.getRoomId(),clj);
                                    }
                                }
                                else if (message.getType().equals("play")){
                                    PlayerMessageModel play=(PlayerMessageModel) message;
                                    if (play.getRoomId().equals(roomID)){
                                        setMovieTime(play.getMessage());
                                        UsersInRoomModel server=new UsersInRoomModel(play.getId(),
                                                play.getUserName(),play.getDeviceOs(),"server",true,
                                                System.currentTimeMillis());
                                        editSpectator(server);
                                        AliveMessageModel aliveMessageModel=new AliveMessageModel(
                                                client.getId(), "alv", client.getDeviceName(),client.getDeviceOs(),
                                                "client",roomID,client.getUserName()
                                        );
                                        sendMessage(roomID,aliveMessageModel);
                                    }

                                }  else if (message.getType().equals("alv") && !message.getId().equals(client.getId())) {
                                    AliveMessageModel alive=(AliveMessageModel) message;
                                    if (alive.getRoomId().equals(roomID)){
                                        editSpectator(
                                                new UsersInRoomModel(alive.getId(),
                                                        alive.getUserName(),
                                                        alive.getDeviceOs(),
                                                        alive.getClientRole(),
                                                        true,
                                                        System.currentTimeMillis()));
                                    }
                                }
//                                    else if (message.getType().equals("clj") && !message.getId().equals(clientId)) {
//                                        ClientJoinedMessageModel clj=(ClientJoinedMessageModel) message;
//                                        if (clj.getRoomId().equals(roomID)){
//                                            UsersInRoomModel client=new UsersInRoomModel(clj.getId(),
//                                                    clj.getUserName(),
//                                                    clj.getDeviceOs(),
//                                                    clj.getClientRole(),true,
//                                                    System.currentTimeMillis());
//                                            addNewSpectator(client);
//                                        }
//
//                                    }
                            }
                        }

                    }
                }
            });

        }catch (MqttException e){
            System.out.println(e.getMessage());
        }
    }
    private void sendMessage(String roomID, MessageModel messageModel) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                jsonString = objectMapper.writeValueAsString(messageModel);
                System.out.println(jsonString);
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
            }
        String secretKey = getString(R.string.secretKey);
        String encJson = roomID + ":" + encrypt(jsonString, secretKey);
            MqttMessage msg = new MqttMessage(encJson.getBytes());
            msg.setQos(2);
            System.out.println("Message sent");
            if (mqttClient!=null){
                if (mqttClient.isConnected()){
                    isClientConnected=true;
                    try {
                        mqttClient.publish("syncplayer/rooms", msg);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    isClientConnected=false;
                    Toast.makeText(this, R.string.errDisconnected, Toast.LENGTH_SHORT).show();
                }
            }
    }
    private String encrypt(String strToEncrypt, String secret) {
        try {
            IvParameterSpec iv = generateIv();
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(iv.getIV(), Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
    @SuppressLint("SourceLockedOrientationActivity")
    private void goPortrait() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewGroup.LayoutParams params = playerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(params);
        isFullscreen = false;
    }
    private void goLandscape() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ViewGroup.LayoutParams params = playerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(params);
        isFullscreen = true;

    }
    @OptIn(markerClass = UnstableApi.class)
    private void SetAudioTrack(int id) {
        if (audioDisabled){
            player.setTrackSelectionParameters(
                    player.getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, /* disabled= */ false)
                            .build());
            audioDisabled =false;
        }
        ImmutableList<Tracks.Group> groups = player.getCurrentTracks().getGroups();
        for (int index=0; index<groups.size();index++){
            Tracks.Group g=groups.get(index);
            if (g.getTrackFormat(0).sampleMimeType != null && g.getTrackFormat(0).sampleMimeType.contains("audio")){
                TrackGroup trackGroup=g.getMediaTrackGroup();
                if (g.getMediaTrackGroup().id.equals(String.valueOf(id))) {
                    TrackSelectionParameters.Builder parametersBuilder = player.getTrackSelectionParameters().buildUpon();
                    parametersBuilder.setOverrideForType(new TrackSelectionOverride(g.getMediaTrackGroup(), trackGroup.length-1));
                    player.setTrackSelectionParameters(parametersBuilder.build());
                    break;
                }
            }
        }
    }
    @OptIn(markerClass = UnstableApi.class)
    private void SetSubtitle(int id) {
        if (subDisabled){
            player.setTrackSelectionParameters(
                    player
                            .getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, /* disabled= */ false)
                            .build());
            subDisabled =false;
        }

        ImmutableList<Tracks.Group> groups = player.getCurrentTracks().getGroups();
        for (int index=0; index<groups.size();index++){
            Tracks.Group g=groups.get(index);
            if (g.getTrackFormat(0).codecs != null && g.getTrackFormat(0).codecs.contains("subrip")){
                TrackGroup trackGroup=g.getMediaTrackGroup();
                if (g.getMediaTrackGroup().id.equals(String.valueOf(id))) {
                    TrackSelectionParameters.Builder parametersBuilder = player.getTrackSelectionParameters().buildUpon();
                    parametersBuilder.setOverrideForType(new TrackSelectionOverride(g.getMediaTrackGroup(), trackGroup.length-1));
                    player.setTrackSelectionParameters(parametersBuilder.build());
                    break;
                }
            }
        }
    }
    private void DisableAudio() {
        player.setTrackSelectionParameters(
                player.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, /* disabled= */ true)
                        .build());
        audioDisabled =true;
    }
    private void DisableSubtitle() {
        player.setTrackSelectionParameters(
                player
                        .getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, /* disabled= */ true)
                        .build());
        subDisabled =true;
    }
    private void loadVideo() {
        String url = urlEditText.getText().toString();
        if (!url.isEmpty()) {
            if (isValidVideoURL(url)){
                MediaItem mediaItem = MediaItem.fromUri(url);
                player.setMediaItem(mediaItem);
                player.prepare();
                movieLInk=url;
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.errMovieLinkVerify, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),R.string.errMovieURLNA, Toast.LENGTH_SHORT).show();
        }
    }
    private void play() {
        player.play();
    }
    private void pause() {
        player.pause();
    }
    private void stop() {
        player.stop();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFullscreen) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
    @Override
    protected void onDestroy() {
        player.release();
        try {
            if (mqttClient.isConnected()){
                mqttClient.disconnect();
                mqttClient.close();
            }
            Toast.makeText(this, R.string.errDisconnected, Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        interruptedWhile =true;
        if (specChecker !=null){
            specChecker.shutdown();
        }
        super.onDestroy();
    }
    private void setMovieLink(String movieLink) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                movieLInk = movieLink;
                if (!movieLink.isEmpty()) {
                    MediaItem mediaItem = MediaItem.fromUri(movieLink);
                    player.setMediaItem(mediaItem);
                    player.prepare();
                    player.play();
                }
            }
        });
    }
    private void setMovieTime(String movieTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG,"Time Received : " + movieTime);
                if (movieTime.equals("paused")) {
                    if (player.isPlaying()) {
                        pause();
                    }
                } else {
                    if (!player.isPlaying()) {
                        play();
                    }
                    if (movieTime.contains(":")) {
                        long movetime = Long.parseLong(movieTime.split(":")[0]);
                        long currenttime = player.getCurrentPosition();
                        long delay=movetime-currenttime;
                        int sub = Integer.parseInt(movieTime.split(":")[1]);
                        int track = Integer.parseInt(movieTime.split(":")[2]);
                        if (subTitle !=null){
                            if (!subTitle.isEmpty()){
                                if (sub != -1 && sub != Integer.parseInt(subTitle)) {
                                    //mediaPlayer.subpictures().setTrack(sub);
                                    SetSubtitle(sub);
                                } else if (sub==-1) {
                                    DisableSubtitle();
                                }
                            }
                        }

                        if (selectedAudio !=null){
                            if (!selectedAudio.isEmpty() ){
                                if (track != -1 && track != Integer.parseInt(selectedAudio)) {
                                    //mediaPlayer.audio().setTrack(track);
                                    SetAudioTrack(track);
                                }
                                else if (track==-1){
                                    DisableAudio();
                                }
                            }
                        }


                        if (delay>-movieTimeDelay && delay< movieTimeDelay) {
                            Log.v(TAG,"Delay : " + delay);
                            Log.v(TAG,"Current Playing Time : "+currenttime);
                        } else {
                            player.seekTo(movetime);
                            Log.v(TAG,"Time Set To: "+movetime);
                            if (!player.isPlaying()){
                                play();
                            }
                        }
                    }
                }
            }
        });
    }
    private void editSpectator(UsersInRoomModel spectator){
        UsersInRoomModel usersInRoom=roomDatabaseHelper.selectData(spectator.getID());

        if (usersInRoom == null) {
            addNewSpectator(spectator);
            return;
        }
        roomDatabaseHelper.updateData(spectator);
        updateRecyclerView();
    }
    private void addNewSpectator(UsersInRoomModel spectator) {
        roomDatabaseHelper.insertData(spectator);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        getApplicationContext(),
                        String.format(
                                getString(R.string.onNewUserJoined),spectator.getNickName()),
                        Toast.LENGTH_SHORT).show();
            }
        });
        updateRecyclerView();
    }
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale currentLocale = getResources().getConfiguration().locale;

        // Check if the current locale is different from the desired locale
        if (!currentLocale.getLanguage().equals(locale.getLanguage())) {
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            recreate(); // Restart activity to apply the new locale
        }

    }
    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "fa"); // Default to Persian
        setLocale(language);

    }
    private static boolean isValidVideoURL(String url) {
        String regex = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]+|https?://\\S+?\\.(mp4|mkv)((\\?\\S*)?(#\\S*)?|(#\\S*)?(\\?\\S*)?)$";
//        Pattern VIDEO_URL_PATTERN = Pattern.compile(
//                "^(http|https):\\/\\/.*\\.(mkv|mp4)$"
//        );
        Pattern VIDEO_URL_PATTERN = Pattern.compile(regex);
        try {
            if (VIDEO_URL_PATTERN.matcher(url).matches()) {
                return true;
            } else {
                throw new IllegalArgumentException("Invalid URL format");
            }
        } catch (Exception e) {
            System.out.println("Validation failed: " + e.getMessage());
            return false;
        }
    }
    private void updateUserCount(int count) {
        if (usersCountLabel != null && usersCountInRoom != null) {
            // Update the text dynamically
            if (isServer){
                usersCountInRoom.setText(String.valueOf(count-1));
            }else{
                usersCountInRoom.setText(String.valueOf(count));
            }
        }
    }
    private void startUserCheck() {
        specChecker.scheduleWithFixedDelay(() -> {
            try {
                List<UsersInRoomModel> users = roomDatabaseHelper.selectAllData();
                if (users.size() > 1) {
                    for (UsersInRoomModel user : users) {
                        if (!user.getID().equals(clientId)) {
                            long elapsedTime = (System.currentTimeMillis() - user.getLastAlive()) / 1000;
                            if (elapsedTime > 10) {
                                System.out.println("User "+user.getNickName()+" is Disconnected");
                                roomDatabaseHelper.deleteData(user);
                                if (user.getClientRole().equals("server")){
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                            R.string.onRoomOwnerDisconnected, Toast.LENGTH_SHORT).show());
                                }else{
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                            String.format(
                                                    getString(
                                                            R.string.onUserDisconnected),user.getNickName()),
                                            Toast.LENGTH_SHORT).show());
                                }
                                updateRecyclerView();
                            } else if (elapsedTime > 5) {
                                System.out.println("User "+user.getNickName()+" is Offline");
                                user.setOnline(false);
                                roomDatabaseHelper.updateData(user);
                                updateRecyclerView();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }, 0, 2, TimeUnit.SECONDS);
    }
    private void updateRecyclerView() {
        runOnUiThread(() -> {
            try {
                usersListAdapter.updateData(roomDatabaseHelper.selectAllData());
                updateUserCount(roomDatabaseHelper.selectAllData().size());
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        });
    }
}