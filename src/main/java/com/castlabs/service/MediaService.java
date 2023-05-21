package com.castlabs.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.castlabs.model.Box;
import org.springframework.stereotype.Service;

@Service
public class MediaService {
    public List<Box> getMediaResult(String url) {
//        String mp4Uri = "https://demo.castlabs.com/tmp/text0.mp4";
        String mp4Uri = url;
        List<Box> result = new ArrayList<>();
        mp4layout(mp4Uri, new Mp4LayoutCallback() {
            @Override
            public void onSuccess(List<Box> boxes, String payload) {
                System.out.println("Boxes:");
                for (Box box : boxes) {
                    System.out.println(box);
                    result.add(box);
                }
                System.out.println("Payload: " + payload.replaceAll(",", "").replaceAll(" ", ""));

            }


            @Override
            public void onFailure(String errorMessage) {
                System.out.println("Error: " + errorMessage);
            }
        });
        return result;
    }

    private static void mp4layout(String mp4Uri, Mp4LayoutCallback callback) {
        readBox(mp4Uri, 0, new ArrayList<>(), callback);
    }

    private static void readBox(String mp4Uri, long offset, List<Box> boxes, Mp4LayoutCallback callback) {
        httpGet(mp4Uri, offset, 8, new HttpGetCallback() {
            @Override
            public void onSuccess(byte[] data) {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                int length = buffer.getInt();
                long boxOffset = offset;
                byte[] boxCodeBytes = new byte[4];
                buffer.get(boxCodeBytes);
                String boxCode = new String(boxCodeBytes, StandardCharsets.ISO_8859_1);
                Box currBox = new Box(length, boxOffset, boxCode);
                boxes.add(currBox);

                if (boxCode.equals("moof") || boxCode.equals("traf")) {
                    long nextOffset = currBox.getOffset() + 8;
                    readBox(mp4Uri, nextOffset, boxes, new Mp4LayoutCallback() {
                        @Override
                        public void onSuccess(List<Box> innerBoxes, String payload) {
                            callback.onSuccess(boxes, payload);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            callback.onFailure(errorMessage);
                        }
                    });
                } else {
                    if (boxCode.equals("mdat")) {
                        rdDone(mp4Uri, currBox.getLength(), currBox.getOffset(), new RdDoneCallback() {
                            int retry = 3;

                            @Override
                            public void onSuccess(byte[] buf) {
                                ByteBuffer dv = ByteBuffer.wrap(buf);
                                List<Character> boxData = new ArrayList<>();
                                for (int i = 8; i < dv.capacity(); i++) {
                                    boxData.add((char) dv.get(i));
                                }
                                String payload = boxData.toString();
                                callback.onSuccess(boxes, payload);
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                if (retry > 0) {
                                    retry--;
                                    rdDone(mp4Uri, currBox.getLength(), currBox.getOffset(), this);
                                } else {
                                    callback.onFailure(errorMessage);
                                }
                            }
                        });
                    } else {
                        readBox(mp4Uri, currBox.getOffset() + currBox.getLength(), boxes, callback);
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    private static void rdDone(String mp4Uri, int length, long offset, RdDoneCallback callback) {
        httpGet(mp4Uri, offset, length, new HttpGetCallback() {
            @Override
            public void onSuccess(byte[] data) {
                callback.onSuccess(data);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    private static void httpGet(String mp4Uri, long offset, int length, HttpGetCallback callback) {
        try {
            URL url = new URL(mp4Uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + offset + "-" + (offset + length - 1));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                InputStream inputStream = connection.getInputStream();
                byte[] data = inputStream.readAllBytes();
                callback.onSuccess(data);
            } else {
                callback.onFailure("HTTP response status (" + responseCode + ") not ok");
            }

            connection.disconnect();
        } catch (IOException e) {
            callback.onFailure(e.getMessage());
        }
    }

    interface Mp4LayoutCallback {
        void onSuccess(List<Box> boxes, String payload);
        void onFailure(String errorMessage);
    }

    interface HttpGetCallback {
        void onSuccess(byte[] data);
        void onFailure(String errorMessage);
    }

    interface RdDoneCallback {
        void onSuccess(byte[] data);
        void onFailure(String errorMessage);
    }


}

