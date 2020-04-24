/**
 * Driver:     Ecowitt WiFi Gateway
 * Author:     Mirco Caramori
 * Repository: https://github.com/mircolino/hubitat/tree/master/ecowitt
 * Import URL: https://raw.githubusercontent.com/mircolino/hubitat/master/ecowitt/ecowitt.groovy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
*/

metadata {
  definition(name: 'Ecowitt WiFi Gateway', namespace: 'mircolino', author: 'Mirco Caramori') {
    capability 'Sensor';

    attribute 'model', 'string';
    attribute 'version', 'string';
  }

  preferences {
    input(name: 'macAddress', type: 'string', title: '<font style="font-size:12px; color:#1a77c9">MAC address</font>', description: '<font style="font-size:12px; font-style: italic">MAC address of your Ecowitt WiFi gateway</font>', defaultValue: '', required: true);
    input(name: 'logLevel', type: 'enum', title: '<font style="font-size:12px; color:#1a77c9">Log verbosity</font>', description: '<font style="font-size:12px; font-style: italic">default is "Debug" for 30 min and "Info" thereafter</font>', options: [0:'Error', 1:'Warning', 2:'Info', 3:'Debug'], multiple: false, defaultValue: 3);
  }
}

// Helpers --------------------------------------------------------------------------------------------------------------------

private String getMacAddress() {
  String mac = null;

  if (settings.macAddress != null) {
    String str = settings.macAddress;
    str = str.replaceAll('[^a-fA-F0-9]', '');
    if (str.length() == 12) mac = str.toUpperCase();
  }

  return (mac);
}

// ------------------------------------------------------------

private void updateDNI() {
  String mac = getMacAddress();

  if (mac) device.deviceNetworkId = mac;  
  else logError('Invalid MAC address');
}

// ------------------------------------------------------------

private int getLogLevel() {
  return ((settings.logLevel != null)? settings.logLevel: 3);
}

// ------------------------------------------------------------

private void logError(String str) {
  if (getLogLevel() >= 0) log.error(str);
}

// ------------------------------------------------------------

private void logWarning(String str) {
  if (getLogLevel() >= 1) log.warn(str);
}

// ------------------------------------------------------------

private void logInfo(String str) {
  if (getLogLevel() >= 2) log.info(str); 
}

// ------------------------------------------------------------

private void logDebug(String str) {
  if (getLogLevel() >= 3) log.debug(str);
}

// ------------------------------------------------------------

void logDebugOff() {
  if (getLogLevel() >= 3) device.updateSetting('logLevel', [type: 'enum', value: 2]);
}

// Lifecycle ------------------------------------------------------------------------------------------------------------------

void installed() {
  logDebug('installed()');
}

// ------------------------------------------------------------

void updated() {
  logDebug('updated()');

  unschedule();

  // Update Device Network ID
  updateDNI();
  
  // Turn off debug log in 30 minutes
  if (getLogLevel() >= 3) runIn(1800, logDebugOff);
}

// ------------------------------------------------------------

void uninstalled() {
  logDebug('uninstalled()');
}

// ------------------------------------------------------------

void parse(String msg) {
  logDebug('parse()');

  Map data = parseLanMessage(msg);

  String body = data['body'];

  data = [:];
  body.split('&').each {
    String[] keyValue = it.split('=');
    data[keyValue[0]] = keyValue[1];
  }

  data.each {
    logInfo("$it.key = $it.value");
  }
}

// EOF ------------------------------------------------------------------------------------------------------------------------
