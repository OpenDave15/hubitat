/**
 * Driver:     Ecowitt Weather Sensor 
 * Author:     Mirco Caramori
 * Repository: https://github.com/mircolino/hubitat/tree/master/ecowitt
 * Import URL: https://raw.githubusercontent.com/mircolino/hubitat/master/ecowitt/ecowitt_wh3x.groovy
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
 * Change Log:
 *
 * 2020.04.25 - Initial implementation
 *
*/

metadata {
  definition(name: "Ecowitt Weather Sensor", namespace: "mircolino", author: "Mirco Caramori") {
    capability "Sensor";

    capability "Battery";
    capability "Temperature Measurement";
    capability "Relative Humidity Measurement";
    capability "Pressure Measurement";

    attribute "pressure_abs", "number";
  }
}

// Logging --------------------------------------------------------------------------------------------------------------------

private void logError(String str) { log.error(str); }
private void logWarning(String str) { if (parent.getLogLevel() > 0) log.warn(str); }
private void logInfo(String str) { if (parent.getLogLevel() > 1) log.info(str); }
private void logDebug(String str) { if (parent.getLogLevel() > 2) log.debug(str); }

// State handling --------------------------------------------------------------------------------------------------------------

void updateStates(String key, String val) {
  //
  // Dispatch state changes to hub
  //
  switch (key) {
  //
  // Integrated/Indoor Weather Sensor
  //
  case "battin":
  case "battout":
    // Eg: battery = 0 (100: OK, 0: Replace / buil-in state from declaring capability "Battery")
    if (state.battery != val) sendEvent(name: "battery", value: val, unit: "%");
    break;

  case "tempinf":
  case "tempf":
    // Eg: temperature = 78.6 (buil-in state from declaring capability "Temperature Measurement")
    if (state.temperature != val) sendEvent(name: "temperature", value: val, unit: "°F");
    break;

  case "humidityin":
  case "humidity":
    // Eg: humidity = 42 (buil-in state from declaring capability "Relative Humidity Measurement")
    if (state.humidity != val) sendEvent(name: "humidity", value: val, unit: "%");
    break;

  case "baromrelin":
    // Eg: pressure = 29.639 (buil-in state from declaring capability "Pressure Measurement")
    if (state.pressure != val) sendEvent(name: "pressure", value: val, unit: "in");
    break;

  case "baromabsin":
    // Eg: pressure_abs = 29.639
    if (state.pressure_abs != val) sendEvent(name: "pressure_abs", value: val, unit: "in");
    break;
  }
}

// Driver lifecycle -----------------------------------------------------------------------------------------------------------

void installed() { logDebug("installed()"); }
void updated() { logDebug("updated()"); }
void uninstalled() { logDebug("uninstalled()"); }
void parse(String msg) { logDebug("parse()"); }

// EOF ------------------------------------------------------------------------------------------------------------------------
