/**
 * @projectDescription	Visual display of PDO results in a timeline format.
 * @inherits	i2b2
 * @namespace	i2b2.Timeline
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 11-06-08: 	Initial Launch [Griffin Weber] 
 * updated 01-13-09:	Performance tuning, added details dialogs [Nick Benik]
 */

i2b2.Timeline.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("Timeline-CONCPTDROP", "CONCPT", op_trgt);
	i2b2.sdx.Master.AttachType("Timeline-PRSDROP", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("Timeline-CONCPTDROP", "CONCPT", "DropHandler", i2b2.Timeline.conceptDropped);
	i2b2.sdx.Master.setHandlerCustom("Timeline-PRSDROP", "PRS", "DropHandler", i2b2.Timeline.prsDropped);
	// array to store concepts
	i2b2.Timeline.model.concepts = [];
	// set initial pagination values
	i2b2.Timeline.model.pgstart = 1;
	i2b2.Timeline.model.pgsize = 10;
	// set initial zoom values
	i2b2.Timeline.model.zoomScale = 1.0;
	i2b2.Timeline.model.zoomPan = 1.0;

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("Timeline-TABS", {activeIndex:0});
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="Timeline-TAB1") {
			// user switched to Results tab
			if ((i2b2.Timeline.model.concepts.length>0) && i2b2.Timeline.model.prsRecord) {
			// contact PDO only if we have data
				if (i2b2.Timeline.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					$('Timeline-pgstart').value = '1';
					$('Timeline-pgsize').value = '10';
					i2b2.Timeline.pgGo(0);
				}
			}
		}
	});
};

i2b2.Timeline.Unload = function() {
	// purge old data
	i2b2.Timeline.model = {};
	i2b2.Timeline.model.prsRecord = false;
	i2b2.Timeline.model.conceptRecord = false;
	i2b2.Timeline.model.dirtyResultsData = true;
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
	return true;
};

i2b2.Timeline.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.Timeline.model.prsRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("Timeline-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("Timeline-PRSDROP").style.background = "#CFB";
	setTimeout("$('Timeline-PRSDROP').style.background='#DEEBEF'", 250);	
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Timeline.model.dirtyResultsData = true;		
};

i2b2.Timeline.conceptDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.Timeline.model.concepts.push(sdxData);
	// sort and display the concept list
	i2b2.Timeline.conceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Timeline.model.dirtyResultsData = true;		
};

i2b2.Timeline.conceptDelete = function(concptIndex) {
	// remove the selected concept
	i2b2.Timeline.model.concepts.splice(concptIndex,1);
	// sort and display the concept list
	i2b2.Timeline.conceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Timeline.model.dirtyResultsData = true;		
};

i2b2.Timeline.Resize = function() {
	var h = parseInt( $('anaPluginViewFrame').style.height ) - 61 - 17;
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-timelineBox")[0].style.height = h + 'px';
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
};

i2b2.Timeline.wasHidden = function() {
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
}	

i2b2.Timeline.conceptsRender = function() {
	var s = '';
	// are there any concepts in the list
	if (i2b2.Timeline.model.concepts.length) {
		// sort the concepts in alphabetical order
		i2b2.Timeline.model.concepts.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of concepts
		for (var i1 = 0; i1 < i2b2.Timeline.model.concepts.length; i1++) {
			if (i1 > 0) { s += '<div class="concptDiv"></div>'; }
			s += '<a class="concptItem" href="JavaScript:i2b2.Timeline.conceptDelete('+i1+');">' + i2b2.h.Escape(i2b2.Timeline.model.concepts[i1].sdxInfo.sdxDisplayName) + '</a>';
		}
		// show the delete message
		$("Timeline-DeleteMsg").style.display = 'block';
	} else {
		// no concepts selected yet
		s = '<div class="concptItem">Drop one or more Concepts here</div>';
		$("Timeline-DeleteMsg").style.display = 'none';
}
	// update html
	$("Timeline-CONCPTDROP").innerHTML = s;
};

i2b2.Timeline.showObservation = function(localkey) {
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
	var t = i2b2.Timeline.model.observation_PKs[localkey];
	var disp =	"\nevent_id: " + t.event_id + "<br />" +
				"\npatient_id: " + t.patient_id + "<br />" +
				"\nconcept_id: " + t.concept_id + "<br />" +
				"\nobserver_id: " + t.observer_id + "<br />" +
				"\nstart_date: " + t.start_date_key;
	i2b2.Timeline.yuiPanel = new YAHOO.widget.Panel("Timeline-InfoPanel", { width:"320px", 
					zindex: 10000, 
					constraintoviewport: true,
					dragOnly: true,
					visible: true, 
					context: ["TIMELINEOBS-"+localkey,"tr","bl", ["beforeShow", "windowResize", "windowScroll"]] } );   
	i2b2.Timeline.yuiPanel.setHeader("Observation Details");
	i2b2.Timeline.yuiPanel.setBody(disp);
	i2b2.Timeline.yuiPanel.render(document.body);
}

i2b2.Timeline.pgGo = function(dir) {
	var formStart = parseInt($('Timeline-pgstart').value);
	var formSize = parseInt($('Timeline-pgsize').value);
	if (!formStart) {formStart = 1;}
	if (!formSize) {formSize = 10;}
	if (formSize<1) {formSize = 1;}
	formStart = formStart + formSize * dir;
	if (formStart<1) {formStart = 1;}
	i2b2.Timeline.model.pgstart = formStart;
	i2b2.Timeline.model.pgsize = formSize;
	$('Timeline-pgstart').value = formStart;
	$('Timeline-pgsize').value = formSize;
	i2b2.Timeline.model.dirtyResultsData = true;
	//remove old results
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-directions")[0].hide();
	$('Timeline-results-scaleLbl1').innerHTML = '';
	$('Timeline-results-scaleLbl2').innerHTML = '';
	$('Timeline-results-scaleLbl3').innerHTML = '';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-timeline")[0].innerHTML = '<div class="results-progress">Please wait while the timeline is being drawn...</div><div class="results-progressIcon"></div>';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-finished")[0].show();		
	//reset zoom key
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.width = '90px';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.left = '0px';
	// give a brief pause for the GUI to catch up
	setTimeout('i2b2.Timeline.getResults();', 50);
};

i2b2.Timeline.updateZoomScaleLabels = function() {
	var z = i2b2.Timeline.model.zoomScale*1.0;
	var p = i2b2.Timeline.model.zoomPan*1.0;
	// update zoom key
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.width = (90/z) + 'px';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.left = ((p*90)-(90/z)) + 'px';
	// calculate date labels
	var first_time = i2b2.Timeline.model.first_time;
	var last_time = i2b2.Timeline.model.last_time;
	var lf = last_time - first_time;
	var t3 = first_time + lf*p;
	var t1 = t3 - lf/z;
	var t2 = (t1+t3)/2;
	var d1 = new Date(t1);
	var d2 = new Date(t2);
	var d3 = new Date(t3);
	// update labels
	$('Timeline-results-scaleLbl1').innerHTML = (d1.getMonth()+1) + '/' + d1.getDate() + '/' + d1.getFullYear();
	$('Timeline-results-scaleLbl2').innerHTML = (d2.getMonth()+1) + '/' + d2.getDate() + '/' + d2.getFullYear();
	$('Timeline-results-scaleLbl3').innerHTML = (d3.getMonth()+1) + '/' + d3.getDate() + '/' + d3.getFullYear();
}

i2b2.Timeline.zoom = function(op) {
	if (op == '+') {
		i2b2.Timeline.model.zoomScale *= 2.0;
	}
	if (op == '-') {
		i2b2.Timeline.model.zoomScale *= 0.5;
	}
	if (op == '<') {
		i2b2.Timeline.model.zoomPan -= 0.25/(i2b2.Timeline.model.zoomScale*1.0);
	}
	if (op == '>') {
		i2b2.Timeline.model.zoomPan += 0.25/(i2b2.Timeline.model.zoomScale*1.0);
	}
	if (i2b2.Timeline.model.zoomScale < 1) {
		i2b2.Timeline.model.zoomScale = 1.0;
	}
	if (i2b2.Timeline.model.zoomPan > 1) {
		i2b2.Timeline.model.zoomPan = 1.0;
	}
	if (i2b2.Timeline.model.zoomPan < 1/(i2b2.Timeline.model.zoomScale*1.0)) {
		i2b2.Timeline.model.zoomPan = 1/(i2b2.Timeline.model.zoomScale*1.0);
	}
	i2b2.Timeline.updateZoomScaleLabels();
	var z = i2b2.Timeline.model.zoomScale*1.0;
	var p = i2b2.Timeline.model.zoomPan*1.0;
	p = 100.0 * (1 - z*p);
	z = 100.0 * z;
	var o = $$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-finished DIV.ptObsZoom");
	for (var i=0; i<o.length; i++) {
		o[i].style.width = z + '%';
		o[i].style.left = p + '%';
	}
};

i2b2.Timeline.getResults = function() {

	if (i2b2.Timeline.model.dirtyResultsData) {
		// translate the concept XML for injection as PDO item XML
		var filterList = '';
		for (var i1=0; i1<i2b2.Timeline.model.concepts.length; i1++) {
			var t = i2b2.Timeline.model.concepts[i1].origData.xmlOrig;
		var cdata = {};
		cdata.level = i2b2.h.getXNodeVal(t, "level");
		cdata.key = i2b2.h.getXNodeVal(t, "key");
		cdata.tablename = i2b2.h.getXNodeVal(t, "tablename");
		cdata.dimcode = i2b2.h.getXNodeVal(t, "dimcode");
		cdata.synonym = i2b2.h.getXNodeVal(t, "synonym_cd");
			filterList +=
			'	<panel name="'+cdata.key+'">\n'+
			'		<panel_number>0</panel_number>\n'+
			'		<panel_accuracy_scale>0</panel_accuracy_scale>\n'+
			'		<invert>0</invert>\n'+
			'		<item>\n'+
			'			<hlevel>'+cdata.level+'</hlevel>\n'+
			'			<item_key>'+cdata.key+'</item_key>\n'+
			'			<dim_tablename>'+cdata.tablename+'</dim_tablename>\n'+
			'			<dim_dimcode>'+cdata.dimcode+'</dim_dimcode>\n'+
			'			<item_is_synonym>'+cdata.synonym+'</item_is_synonym>\n'+
			'		</item>\n'+
			'	</panel>\n';
		}

		var pgstart = i2b2.Timeline.model.pgstart;
		var pgend = pgstart + i2b2.Timeline.model.pgsize - 1;
		var msg_filter = '<input_list>\n' +
			'	<patient_list max="'+pgend+'" min="'+pgstart+'">\n'+
			'		<patient_set_coll_id>'+i2b2.Timeline.model.prsRecord.sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n'+
			'	</patient_list>\n'+
			'</input_list>\n'+
			'<filter_list>\n'+
				filterList+
			'</filter_list>\n'+
			'<output_option>\n'+
			'	<patient_set select="using_input_list" onlykeys="false"/>\n'+
			'	<observation_set blob="false" onlykeys="false"/>\n'+
			'</output_option>\n';


		// callback processor
		var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.scope = this;
		scopedCallback.callback = function(results) {
			// THIS function is used to process the AJAX results of the getChild call
			//		results data object contains the following attributes:
			//			refXML: xmlDomObject <--- for data processing
			//			msgRequest: xml (string)
			//			msgResponse: xml (string)
			//			error: boolean
			//			errorStatus: string [only with error=true]
			//			errorMsg: string [only with error=true]
			
			// check for errors
			if (results.error) {
				alert('The results from the server could not be understood.  Press F12 for more information.');
				console.error("Bad Results from Cell Communicator: ",results);
				return false;
			}

			var s = '';

			var patients = {};
			
			// get all the patient records
			var pData = i2b2.h.XPath(results.refXML, '//patient');

			for (var i1=0; i1<pData.length; i1++) {
				var patientID = i2b2.h.getXNodeVal(pData[i1], "patient_id");
				var patientName = '';
				patientName += 'Person_#';
				patientName += patientID;
				var sex_cd = i2b2.h.XPath(pData[i1], 'descendant-or-self::param[@name="sex_cd"]/text()');
				if (sex_cd.length) {
					patientName += '__';
					var sex_cd_val = sex_cd[0].nodeValue;
					if (sex_cd_val == 'M') {sex_cd_val = 'Male';}
					if (sex_cd_val == 'F') {sex_cd_val = 'Female';}
					if (sex_cd_val == 'U') {sex_cd_val = 'Unknown';}
					patientName += sex_cd_val;
				}
				var age_in_years = i2b2.h.XPath(pData[i1], 'descendant-or-self::param[@name="age_in_years_num"]/text()');
				if (age_in_years.length) {
					patientName += '__';
					patientName += age_in_years[0].nodeValue + 'yrold';
				}
				var race_cd = i2b2.h.XPath(pData[i1], 'descendant-or-self::param[@name="race_cd"]/text()');
				if (race_cd.length) {
					patientName += '__';
					patientName += race_cd[0].nodeValue.substring(0,1).toUpperCase() + race_cd[0].nodeValue.substring(1);
				}
				patients[patientID] = {};
				patients[patientID].name = patientName;
				patients[patientID].concepts = [];
				for (var i2=0; i2<i2b2.Timeline.model.concepts.length; i2++) {
					patients[patientID].concepts[i2] = [];
				}
			}
			
			// get all the observations
			var first_date = new Date('1/1/2500');
			var last_date = new Date('1/1/1500');
			var osData = i2b2.h.XPath(results.refXML, '//observation/..');
			for (var i1=0; i1<osData.length; i1++) {
				var oData = i2b2.h.XPath(osData[i1], 'descendant::observation');
				for (var i2=0; i2<oData.length; i2++) {
					var patientID = i2b2.h.getXNodeVal(oData[i2], "patient_id");
					var o = {};
					o.event_id = i2b2.h.getXNodeVal(oData[i2], "event_id");
					o.concept_cd = i2b2.h.getXNodeVal(oData[i2], "concept_cd");
					o.observer_id = i2b2.h.getXNodeVal(oData[i2], "observer_cd");
					o.start_date_key = i2b2.h.getXNodeVal(oData[i2], "start_date");
					var d = i2b2.h.getXNodeVal(oData[i2], "start_date");
					if (d) { d = d.match(/^[0-9\-]*/).toString(); }
					if (d) { d = d.replace(/-/g,'/'); }
					if (d) { d = new Date(Date.parse(d)); }
					if (d) { o.start_date = d; }
					d = i2b2.h.getXNodeVal(oData[i2], "end_date");
					if (d) { d = d.match(/^[0-9\-]*/).toString(); }
					if (d) { d = d.replace(/-/g,'/'); }
					if (d) { d = new Date(Date.parse(d)); }
					if (d) { o.end_date = d; }
					if ( o.concept_cd && o.start_date && o.end_date && patients[patientID]) {
						patients[patientID].concepts[i1].push(o);
						if (o.start_date < first_date) {first_date = o.start_date;}
						if (o.end_date > last_date) {last_date = o.end_date;}
					}
				}
			}
			
			//i2b2.Timeline.model.patients = patients;
			
			var first_time = first_date.getTime()*1.0;
			var last_time = last_date.getTime()*1.0;
			var lf = last_time - first_time + 1;
			
			i2b2.Timeline.model.first_time = first_time;
			i2b2.Timeline.model.last_time = last_time;
			
			var observation_keys = new Array();
			for (var patientID in patients) {
				s += '<div class="ptBox">';
				s += '<div class="ptName">' + patients[patientID].name + '</div>';
				s += '<table class="ptData">';
				for (i1=0; i1<i2b2.Timeline.model.concepts.length; i1++) {
					if (patients[patientID].concepts[i1].length) {
						s += '<tr>';
						s += '<td class="ptPanel">' + i2b2.h.Escape(i2b2.Timeline.model.concepts[i1].sdxInfo.sdxDisplayName) + '</td>';
						s += '<td class="ptObsTD" valign="top"><div class="spacer">&nbsp;</div>';
						s += '<div class="ptObsDIV">';
						s += '<div class="ptObsBack"></div>';
						s += '<div class="ptObsLine"></div>';
						s += '<div class="ptObs">';
						s += '<div class="ptObsZoom">';
						for (i2=0; i2<patients[patientID].concepts[i1].length; i2++) {
							var d1 = patients[patientID].concepts[i1][i2].start_date;
							var d2 = patients[patientID].concepts[i1][i2].end_date;
							var w = (d1.getTime() - first_time)/lf;
							var w2 = (d2.getTime() - first_time)/lf;
							// used to lookup the primary DB key when an observation is clicked
							var obs_keyval = {
								event_id: patients[patientID].concepts[i1][i2].event_id,
								patient_id: patientID,
								concept_id: patients[patientID].concepts[i1][i2].concept_cd,
								observer_id: patients[patientID].concepts[i1][i2].observer_id,
								start_date_key: patients[patientID].concepts[i1][i2].start_date_key
							};
							observation_keys.push(obs_keyval);
							var obs_key = observation_keys.length - 1;

							if ( (w<=1) && (w2<=1) ) {
								if (w2 > w) {
									s += '<div class="ptOb2" style="left:' + (100*w) + '%;width:' + (100*(w2-w)) + '%;"></div>';
								}
								s += '<a id="TIMELINEOBS-'+obs_key+'" title="'+ i2b2.h.Escape(obs_keyval.concept_id) +'" href="Javascript:i2b2.Timeline.showObservation('+ obs_key +');" class="ptOb" style="left:' + (100*w) + '%;"></a>';
							}
						}
						s += '</div>';
						s += '</div>';
						s += '</div>';
						s += '</td>';
						s += '</tr>';
					}
				}
				
				// save the DB key lookup table
				i2b2.Timeline.model.observation_PKs = observation_keys;
				
				
				s += '</table>';
				s += '</div>';
			}

			i2b2.Timeline.model.zoomScale = 1.0;
			i2b2.Timeline.model.zoomPan = 1.0;
			i2b2.Timeline.updateZoomScaleLabels();
			
			$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-timeline")[0].innerHTML = s;

			// optimization - only requery when the input data is changed
			i2b2.Timeline.model.dirtyResultsData = false;		
		}
		
		// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
		i2b2.CRC.ajax.getPDO_fromInputList("Plugin:Timeline", {PDO_Request:msg_filter}, scopedCallback);
	}
}
