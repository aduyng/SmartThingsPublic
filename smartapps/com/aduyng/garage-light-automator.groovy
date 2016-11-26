/**
 *  Garage Light Automator
 *
 *  Copyright 2016 Duy Nguyen &lt;aduyng@gmail.com&gt;
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Garage Light Automator",
    namespace: "com.aduyng.garagelightautomator",
    author: "Duy Nguyen &lt;aduyng@gmail.com&gt;",
    description: "Turn on or off garage light when you are arriving or leaving",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@3x.png")


preferences {
	section("Garage Interior Open/Close Sensor") {
		input "interiorDoorOpenCloseSensor", "capability.contactSensor", required: true, title: "Which one?"
	}

    section("Garage Open/Close Sensor") {
		input "garageDoorOpenCloseSensor", "capability.contactSensor", required: true, title: "Which one?"
	}

    section("Garage Light Switch") {
		input "theSwitch", "capability.switch", required: true, multiple: false, title: "Which one?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(interiorDoorOpenCloseSensor, "contact", onInteriorDoorContacted)
    subscribe(garageDoorOpenCloseSensor, "contact", onGarageDoorContacted)
    state.mode = ""
}

def onInteriorDoorContacted(evt){
	log.debug "on interior door opened: $evt, current mode: ${location.currentMode}"
	if( "night" == location.currentMode.toLowerCase() ){
        if( "" == state.mode ){
            state.mode = "leaving"
        }

        if( "open" == evt.value && "leaving" == state.mode ){
            log.debug "leaving: interior door opened, switch ON the light"
            theSwitch.on()
        } else if( "closed" == evt.value && "arriving" == state.mode ){
            log.debug "arriving: interior door close, switch OFF the light"
            theSwitch.off()
            state.mode = ""
        }
    }
}

def onGarageDoorContacted(evt){
	log.debug "on garage door opened: $evt"
    if( "night" == location.currentMode.toLowerCase() ){
        if( "" == state.mode){
            state.mode = "arriving"
        }

        if( "closed" == evt.value && "leaving" == state.mode ){
            log.debug "leaving: garage door closed, switch OFF the light"
            theSwitch.off()
            state.mode = ""
        }else if( "open" == evt.value && "arriving" == state.mode ){
            log.debug "arriving: garage door opened, switch ON the light"
            theSwitch.on()
        }
    }
}
