/**
 * @projectDescription	The Asynchronous Query Status controller (GUI-only controller).
 * @inherits 	i2b2.CRC.ctrlr
 * @namespace	i2b2.CRC.ctrlr.QueryStatus
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.0
 * ----------------------------------------------------------------------------------------
 * updated 8-10-09: Initial Creation [Nick Benik] 
 */

i2b2.CRC.ctrlr.QueryStatus = function(dispDIV) { this.dispDIV = dispDIV; };

i2b2.CRC.ctrlr.QueryStatus._GetTitle = function(resultType, oRecord, oXML) {
	var title = "";
	switch (resultType) {
		case "PATIENTSET":
			// use given title if it exist otherwise generate a title
			try {
				var t = i2b2.h.XPath(oXML,'self::query_result_instance/description')[0].firstChild.nodeValue;
			} catch(e) {
				var t = null;
			}
			if (!t) { t = "Patient Set"; }
			// create the title using shrine setting
			if (oRecord.size >= 10) {
				if (i2b2.PM.model.userRoles.length == 1 && i2b2.PM.model.userRoles[0] == "DATA_OBFSC") {
					title = t+" - "+oRecord.size+"&plusmn;3 patients";
				} else {
					title = t+" - "+oRecord.size+" patients";
				}
			} else {
				if (i2b2.PM.model.userRoles.length == 1 && i2b2.PM.model.userRoles[0] == "DATA_OBFSC") {
					title = t+" - 10 patients or less";
				} else {
					title = t+" - "+oRecord.size+" patients";
				}
			}
			break;
		case "PATIENT_COUNT_XML":
			// use given title if it exist otherwise generate a title
			try {
				var t = i2b2.h.XPath(oXML,'self::query_result_instance/description')[0].firstChild.nodeValue;
			} catch(e) {
				var t = null;
			}
			if (!t) { t="Patient Count"; }
			// create the title using shrine setting
			if (oRecord.size >= 10) {
				if (i2b2.PM.model.userRoles.length == 1 && i2b2.PM.model.userRoles[0] == "DATA_OBFSC") {
					title = t+" - "+oRecord.size+"&plusmn;3 patients";
				} else {
					title = t+" - "+oRecord.size+" patients";
				}
			} else {
				if (i2b2.PM.model.userRoles.length == 1 && i2b2.PM.model.userRoles[0] == "DATA_OBFSC") {
					title = t+" - 10 patients or less";
				} else {
					title = t+" - "+oRecord.size+" patients";
				}
			}
	}

	return title;
};

i2b2.CRC.ctrlr.QueryStatus.prototype = function() {
	var private_singleton_isRunning = false;
	var private_startTime = false; 
	var private_refreshInterrupt = false;
		
	function private_pollStatus() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		// this is a private function that is used by all QueryStatus object instances to check their status
		// callback processor to check the Query Instance
		var scopedCallbackQI = new i2b2_scopedCallback();
		scopedCallbackQI.scope = self;
		scopedCallbackQI.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				// find our query instance
				var qi_list = results.refXML.getElementsByTagName('query_instance');
				var l = qi_list.length;
				for (var i=0; i<l; i++) {
					var temp = qi_list[i];
					var qi_id = i2b2.h.XPath(temp, 'descendant-or-self::query_instance_id')[0].firstChild.nodeValue;
					if (qi_id == this.QI.id) {
						// found the query instance, extract the info
						this.QI.status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
						this.QI.statusID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
						if (this.QI.status == "INCOMPLETE") {
							// another poll is required
							setTimeout("i2b2.CRC.ctrlr.currentQueryStatus.pollStatus()", this.polling_interval);
						} else {
							private_singleton_isRunning = false;
							// force a final redraw
							i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus();
							// refresh the query history window
							i2b2.CRC.ctrlr.history.Refresh();
						}
						break;
					}
				}
			}
		}
		

		// callback processor to check the Query Result Set
		var scopedCallbackQRS = new i2b2_scopedCallback();
		scopedCallbackQRS.scope = self;
		scopedCallbackQRS.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				// find our query instance
				var qrs_list = results.refXML.getElementsByTagName('query_result_instance');
				var l = qrs_list.length;
				for (var i=0; i<l; i++) {
					var temp = qrs_list[i];
					var qrs_id = i2b2.h.XPath(temp, 'descendant-or-self::result_instance_id')[0].firstChild.nodeValue;
					if (self.QRS.hasOwnProperty(qrs_id)) {
						var rec = self.QRS[qrs_id];
					} else {
						var rec = new Object();
						rec.QRS_ID = qrs_id;
						rec.size = i2b2.h.getXNodeVal(temp, 'set_size');
						rec.QRS_Type = i2b2.h.XPath(temp, 'descendant-or-self::query_result_type/name')[0].firstChild.nodeValue;
						rec.QRS_TypeID = i2b2.h.XPath(temp, 'descendant-or-self::query_result_type/result_type_id')[0].firstChild.nodeValue;
					}
					rec.QRS_Status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
					rec.QRS_Status_ID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
					// create execution time string
					var d = new Date();
					var t = Math.floor((d.getTime() - private_startTime)/100)/10;
					var exetime = t.toString();
					if (exetime.indexOf('.') < 0) {
						exetime += '.0';
					}
					// deal with time/status setting
					if (!rec.QRS_time) { rec.QRS_time = exetime; }
					if (rec.QRS_Status == "INCOMPLETE" || rec.QRS_Status == "WAITTOPROCESS" || rec.QRS_Status == "PROCESSING") {
						// increment the running time only for parts that are still pending/processing
						rec.QRS_time = exetime;
					}
					// set the proper title if it was not already set
					if (!rec.title) {
						rec.title = i2b2.CRC.ctrlr.QueryStatus._GetTitle(rec.QRS_Type, rec, temp);
					}				
					self.QRS[qrs_id] = rec;
				}
				// see if we need to poll another time
				if (self.QI.status == "INCOMPLETE") { 
					setTimeout("i2b2.CRC.ctrlr.currentQueryStatus.pollStatus()", self.polling_interval);
				} else {
					// refresh the query history window
					i2b2.CRC.ctrlr.history.Refresh();
				}
			}
			// force a redraw
			i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus();
		}
		
		// fire off the ajax calls
		i2b2.CRC.ajax.getQueryInstanceList_fromQueryMasterId("CRC:QueryStatus", {qm_key_value: self.QM.id}, scopedCallbackQI);
		i2b2.CRC.ajax.getQueryResultInstanceList_fromQueryInstanceId("CRC:QueryStatus", {qi_key_value: self.QI.id}, scopedCallbackQRS);
	}
	
	function private_refresh_status() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		// this private function refreshes the display DIV
		if (private_singleton_isRunning) {
			var dispMsg = '<div style="clear:both;"><div style="float:left; font-weight:bold">Running Query: "'+self.QM.name+'"</div>';
			// display the current run duration
			var d = new Date();
			var t = Math.floor((d.getTime() - private_startTime)/100)/10;
			var s = t.toString();
			if (s.indexOf('.') < 0) {
				s += '.0';
			}
			dispMsg += '<div style="float:right">['+s+' secs]</div>';
		} else {
			var dispMsg = '<div style="clear:both;"><div style="float:left; font-weight:bold">Finished Query: "'+self.QM.name+'"</div>';
		}
		dispMsg += '</div>';
		for (var i in self.QRS) {
			var rec = self.QRS[i];			
			dispMsg += '<div style="margin-left:20px; clear:both; height:16px; line-height:16px; "><div style="float:left; height:16px; line-height:16px; ">'+rec.title+'</div>';
			if (rec.QRS_time) {
				var t = '<font color="';
				switch(rec.QRS_Status) {
					case "ERROR":
						t += '#dd0000">ERROR';
						break;
					case "COMPLETED":
					case "FINISHED":
						t += '#0000dd">'+rec.QRS_Status;
						break;
					case "INCOMPLETE":
					case "WAITTOPROCESS":
					case "PROCESSING":
						t += '#00dd00">'+rec.QRS_Status;
						break;
				}
				t += '</font> ';
				dispMsg += '<div style="float:right; height:16px; line-height:16px; ">'+t+'['+rec.QRS_time+' secs]</div>';
			}
			dispMsg += '</div>';
		}
		self.dispDIV.innerHTML = dispMsg;
		//self.dispDIV.style.backgroundColor = '#F00';
		self.dispDIV.style.display = 'none';
		self.dispDIV.style.display = 'block';

		if (!private_singleton_isRunning && private_refreshInterrupt) {
			// make sure our refresh interrupt is turned off
			try {
				clearInterval(private_refreshInterrupt);
				private_refreshInterrupt = false;
			} catch (e) {}
		}
	}

	
	function private_startQuery() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		if (private_singleton_isRunning) { return false; }
		private_singleton_isRunning = true;
		self.dispDIV.innerHTML = '<b>Processing Query: "'+this.name+'"</b>';
		self.QM.name = this.name; 
		self.QRS = {};
		
		// callback processor to run the query from definition
		this.callbackQueryDef = new i2b2_scopedCallback();
		this.callbackQueryDef.scope = this;
		this.callbackQueryDef.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				//		"results" object contains the following attributes:
				//			refXML: xmlDomObject <--- for data processing
				//			msgRequest: xml (string)
				//			msgResponse: xml (string)
				//			error: boolean
				//			errorStatus: string [only with error=true]
				//			errorMsg: string [only with error=true]
				// save the query master
				var temp = results.refXML.getElementsByTagName('query_master')[0];
				self.QM.id = i2b2.h.getXNodeVal(temp, 'query_master_id');
				self.QM.name = i2b2.h.XPath(temp, 'descendant-or-self::name')[0].firstChild.nodeValue;

				// save the query instance
				var temp = results.refXML.getElementsByTagName('query_instance')[0];
				self.QI.id = i2b2.h.XPath(temp, 'descendant-or-self::query_instance_id')[0].firstChild.nodeValue;
				self.QI.status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
				self.QI.statusID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
				
				// we don't need to poll, all Result instances are listed in this message
				if (false && (self.QI.status == "COMPLETED" || self.QI.status == "ERROR")) {
					// create execution time string
					var d = new Date();
					var t = Math.floor((d.getTime() - private_startTime)/100)/10;
					var exetime = t.toString();
					if (exetime.indexOf('.') < 0) {
						exetime += '.0';
					}
					var qi_list = results.refXML.getElementsByTagName('query_result_instance');
					var l = qi_list.length;
					for (var i=0; i<l; i++) {
						try {
							var qi = qi_list[i];
							var temp = new Object();
							temp.size = i2b2.h.getXNodeVal(qi, 'set_size');
							temp.QI_ID = i2b2.h.getXNodeVal(qi, 'query_instance_id');
							temp.QRS_ID = i2b2.h.getXNodeVal(qi, 'result_instance_id');
							temp.QRS_Type = i2b2.h.XPath(qi, 'descendant-or-self::query_result_type/name')[0].firstChild.nodeValue;
							temp.QRS_TypeID = i2b2.h.XPath(qi, 'descendant-or-self::query_result_type/result_type_id')[0].firstChild.nodeValue;
							temp.QRS_Status = i2b2.h.XPath(qi, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
							temp.QRS_Status_ID = i2b2.h.XPath(qi, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
							temp.QRS_time = exetime;
							// set the proper title if it was not already set
							if (!temp.title) {
								temp.title = i2b2.CRC.ctrlr.QueryStatus._GetTitle(temp.QRS_Type, temp, qi);
							}
							self.QRS[temp.QRS_ID] = temp;
						} catch	(e) {}
					}
					private_singleton_isRunning = false;
				} else {
					// another poll is required
					setTimeout("i2b2.CRC.ctrlr.currentQueryStatus.pollStatus()", this.polling_interval);
				}				
			}
		}
		
		// switch to status tab
		i2b2.CRC.view.status.showDisplay();

		// timer and display refresh stuff
		private_startTime = new Date();
		private_refreshInterrupt = setInterval("i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus()", 100);

		// AJAX call
		i2b2.CRC.ajax.runQueryInstance_fromQueryDefinition("CRC:QueryTool", this.params, this.callbackQueryDef);
	}
	return {
		name: "",
		polling_interval: 1000,
		QM: {id:false, status:""},
		QI: {id:false, status:""},
		QRS:{},
		displayDIV: false,
		running: false,
		started: false,
		startQuery: function(queryName, ajaxParams) {
			this.name = queryName;
			this.params = ajaxParams;
			private_startQuery.call(this);
		},
		isQueryRunning: function() {
			return private_singleton_isRunning;
		},
		refreshStatus: function() {
			private_refresh_status();
		},
		pollStatus: function() {
			private_pollStatus();
		}
	};
}();

i2b2.CRC.ctrlr.currentQueryStatus = false; 

