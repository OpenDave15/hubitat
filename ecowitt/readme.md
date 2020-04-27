# <span class="c8">Ecowitt WiFi Gateway</span>

### <span class="c4">Installation Instructions</span>

<span class="c0"></span>

1.  <span class="c0">In "Hubitat → Drivers Code" add new drivers for the Gateway and all the Sensors you want to be supported.</span>
2.  <span class="c0">In "Hubitat → Devices" add a new "Ecowitt WiFi Gateway" virtual device.</span>
3.  <span class="c0">Enter the Gateway MAC address (in any legal form) in the preferences.</span>
4.  <span class="c0">Click <Save Preferences></span>
5.  <span class="c0">The first time the hub will receive data from the Gateway, the driver will automatically create child devices for all the present (and supported) sensors (depending on the frequency you setup your Gateway to POST, this may take a few minutes).</span>

### <span class="c4">Disclaimer</span>

<span class="c0"></span>

<span>This driver is still under development. Install it on a production Hubitat hub at your own risk.</span>
