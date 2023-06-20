import 'dart:async';
import 'package:flutter/services.dart';
import 'src/android_info_model.dart';
export 'src/android_info_model.dart';

/// Provides device and operating system information.
class AndroidInfoPlugin {
  /// No work is done when instantiating the plugin. It's safe to call this
  /// repeatedly or in performance-sensitive blocks.
  AndroidInfoPlugin();

  /// This information does not change from call to call. Cache it.
  AndroidDeviceInfo? _cachedAndroidDeviceInfo;

  Future<AndroidDeviceInfo> get androidInfo async =>
      _cachedAndroidDeviceInfo ??= AndroidDeviceInfo.fromMap(
          (await _MethodChannelDeviceInfo.deviceInfo()));
}

class _MethodChannelDeviceInfo {
  /// The method channel used to interact with the native platform.
  _MethodChannelDeviceInfo._();
  static const MethodChannel _channel =
      MethodChannel('pkg.rahulsharmadev/android_info');

  static Future<Map<String, dynamic>> deviceInfo() async {
    return (await _channel.invokeMethod('getAndroidInfo'))
        .cast<String, dynamic>();
  }
}
