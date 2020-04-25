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
 * Version:    0.1.0   Initial implementation
 *
*/

metadata {
  definition(name: 'Ecowitt WiFi Gateway', namespace: 'mircolino', author: 'Mirco Caramori') {
    capability 'Sensor';

    attribute 'model', 'string';
    attribute 'version', 'string';
  }

  preferences {
    input(name: 'macAddress', type: 'string', title: '<font style="font-size:12px; color:#1a77c9">MAC address</font>', description: '<font style="font-size:12px; font-style: italic">Ecowitt WiFi gateway MAC address</font>', defaultValue: '', required: true);
    input(name: 'logLevel', type: 'enum', title: '<font style="font-size:12px; color:#1a77c9">Log verbosity</font>', description: '<font style="font-size:12px; font-style: italic">Default: "Debug" for 30 min and "Info" thereafter</font>', options: [0:'Error', 1:'Warning', 2:'Info', 3:'Debug'], multiple: false, defaultValue: 3, required: true);
  }
}

// MAC & DNI ------------------------------------------------------------------------------------------------------------------

private String getMacAddress() {
  //
  // Get the Ecowitt MAC address from the driver preferences and validate it
  // Return null is invalid
  //
  if (settings.macAddress != null) {
    String str = settings.macAddress;
    str = str.replaceAll('[^a-fA-F0-9]', '');
    if (str.length() == 12) return (str.toUpperCase());
  }

  return (null);
}

// ------------------------------------------------------------

private void updateDNI() {
  //
  // Get the Ecowitt MAC address and, if valid, update the driver DNI
  //
  String mac = getMacAddress();

  if (mac) device.deviceNetworkId = mac;  
  else logError('The MAC address entered in the driver preferences is invalid');
}

// Logging --------------------------------------------------------------------------------------------------------------------

private int getLogLevel() {
  //
  // Get the log level as an Integer:
  //
  //   0) log only Errors
  //   1) log Errors and Warnings
  //   2) log Errors, Warnings and Info
  //   3) log Errors, Warnings, Info and Debug (everythnig)
  //
  // If the level is not yet set in the driver preferences, return a default of 2 (Info)
  //
  if (settings.logLevel != null) return (settings.logLevel as Integer);
  return (2);
}

// ------------------------------------------------------------

private void logError(String str) {
  log.error(str);
}

// ------------------------------------------------------------

private void logWarning(String str) {
  if (getLogLevel() > 0) log.warn(str);
}

// ------------------------------------------------------------

private void logInfo(String str) {
  if (getLogLevel() > 1) log.info(str); 
}

// ------------------------------------------------------------

private void logDebug(String str) {
  if (getLogLevel() > 2) log.debug(str);
}

// ------------------------------------------------------------

void logDebugOff() {
  //
  // runIn() callback to disable Debug logging after 30 minutes
  // Cannot be private
  //
  if (getLogLevel() > 2) device.updateSetting('logLevel', [type: 'enum', value: '2']);
}

// Lifecycle ------------------------------------------------------------------------------------------------------------------

void installed() {
  //
  // Called once when the driver is created
  //
  try {

  }
  catch (Exception e) {
    logError("Exception in installed(): ${e}");
  }
}

// ------------------------------------------------------------

void updated() {
  //
  // Called everytime the user saves the driver preferences
  //
  try {
    // Unschedule possible previous runIn() calls
    unschedule();

    // Update Device Network ID
    updateDNI();
  
    // Turn off debug log in 30 minutes
    if (getLogLevel() > 2) runIn(1800, logDebugOff);
  }
  catch (Exception e) {
    logError("Exception in updated(): ${e}");
  }
}

// ------------------------------------------------------------

void uninstalled() {
  //
  // Called once when the driver is deleted
  //
  try {

  }
  catch (Exception e) {
    logError("Exception in uninstalled(): ${e}");
  }
}

// ------------------------------------------------------------

void parse(String msg) {
  //
  // Called everytime a POST message is received from the Ecowitt WiFi Gateway
  //
  try {
    // Parse POST message
    Map data = parseLanMessage(msg);

    // Save only the body and discard the header
    String body = data['body'];

    // Build a map with one key/value pair for each field we receive
    data = [:];
    body.split('&').each {
      String[] keyValue = it.split('=');
      data[keyValue[0]] = keyValue[1];
    }

    data.each {
      logDebug("$it.key = $it.value");
    }

  }
  catch (Exception e) {
    logError("Exception in parse(): ${e}");
  }
}

// EOF ------------------------------------------------------------------------------------------------------------------------
