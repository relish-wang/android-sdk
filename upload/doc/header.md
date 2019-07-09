reqBuilder.header(clienttype, 102);
reqBuilder.header(organize, 0);
reqBuilder.header(randomseed, fdf35610d8b34e98);
reqBuilder.header(apptype, 102);
reqBuilder.header(root, 6);
reqBuilder.header(accesstoken, xnwcro-1562297747810);
reqBuilder.header(appversion, 1.2.2);
reqBuilder.header(devicetoken, 358522080749634);
reqBuilder.header(osversion, 9);
reqBuilder.header(deviceinfo, samsung SM-N9500);
reqBuilder.header(user-agent, {"appName":"101","appVersion":"1.2.2","systemName":"Android","deviceVersion":"samsung SM-N9500","systemVersion":"9"});
reqBuilder.header(Content-Type, application/x-www-form-urlencoded);
reqBuilder.header(Content-Length, 0);
reqBuilder.header(Host, pre.api.iotrack.cn);
reqBuilder.header(Connection, Keep-Alive);
reqBuilder.header(Accept-Encoding, gzip);




            @SuppressWarnings("unused")
            private synchronized boolean upload() {
                final String key = model.getKey();
                ResponseInfo responseInfo = mManager.syncPut(
                        model.getLocalPath(),
                        key,
                        mToken,
                        new UploadOptions(
                                null/*map*/,
                                null,
                                false,
                                (key13, percent) -> {
                                    // 上传进度更新
                                    model.setProgress(percent);
                                    long operationTime = model.getOperationTime();
                                    long delta = System.currentTimeMillis() - operationTime;
                                    // B / s
                                    double v = model.getFileSize()
                                            * (percent - model.getOperationTimeProcess())
                                            / (delta / 1000.0);
                                    model.setSpeed(v);
                                    Utils.Log.d(
                                            "progress = " + percent + " " +
                                                    "time = " + (delta / 1000) + " " +
                                                    "speed = " + v + " B/s");
                                    updateFileModel(model);
                                }, () -> {
                            boolean paused = isPaused(key);
                            if (paused) {
                                model.setStatus(FileStatus.PAUSED);
                                updateFileModel(model);
                            }
                            return paused || isRemoved(key);
                        }));
                if (responseInfo.statusCode == ResponseInfo.InvalidToken) {
                    if (isRetry) return false;
                    fetchToken();
                    isRetry = true;
                    return upload();
                }
                if (responseInfo.isNetworkBroken()) {
                    model.setStatus(FileStatus.FAILED);
                    updateFileModel(model);
                    return true;
                } else if (responseInfo.isOK()) {
                    String path;
                    try {
                        path = responseInfo.response.getString("key");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        model.setStatus(FileStatus.FAILED);
                        updateFileModel(model);
                        return false;
                    }
                    model.setUrl(String.format("https://oss.zhihanyun.com/%s", path));
                    model.setStatus(FileStatus.COMPLETED);
                    updateFileModel(model);
                    return true;
                }
                return false;
            }