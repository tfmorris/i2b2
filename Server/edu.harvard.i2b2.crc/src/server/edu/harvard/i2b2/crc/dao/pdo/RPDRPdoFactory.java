/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.datavo.pdo.BlobType;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.datavo.pdo.EidType;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;
import edu.harvard.i2b2.crc.datavo.pdo.PidType;
import edu.harvard.i2b2.crc.datavo.pdo.PidType.PatientId;

/**
 * Class to build individual sections of table pdo xml like
 * patient,concept,observationfact from the given {@link java.sql.ResultSet}
 * $Id: RPDRPdoFactory.java,v 1.20 2009/11/14 16:41:26 rk903 Exp $
 * 
 * @author rkuttan
 */
public class RPDRPdoFactory {
	private static DTOFactory dtoFactory = new DTOFactory();

	/**
	 * Inner class to build observation fact in table PDO format
	 */
	public static class ObservationFactBuilder {
		boolean obsFactDetailFlag = false;
		boolean obsFactBlobFlag = false;
		boolean obsFactStatusFlag = false;

		public ObservationFactBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.obsFactDetailFlag = detailFlag;
			this.obsFactBlobFlag = blobFlag;
			this.obsFactStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build observation fact
		 * 
		 * @param rowSet
		 * @param source
		 * @return ObservationSet.Observation
		 * @throws SQLException
		 * @throws IOException
		 */
		public ObservationType buildObservationSet(ResultSet rowSet,
				String source) throws SQLException, IOException {

			ObservationType observation = new ObservationType();
			PatientIdType pId = new PatientIdType();
			pId.setValue(rowSet.getString("obs_patient_num"));
			pId.setSource(source);
			observation.setPatientId(pId);

			ObservationType.EventId eventId = new ObservationType.EventId();
			eventId.setValue(rowSet.getString("obs_encounter_num"));
			eventId.setSource(source);
			observation.setEventId(eventId);

			ObservationType.ConceptCd conceptCd = new ObservationType.ConceptCd();
			conceptCd.setValue(rowSet.getString("obs_concept_cd"));
			conceptCd.setName(rowSet.getString("concept_name"));
			observation.setConceptCd(conceptCd);

			ObservationType.ModifierCd modifierCd = new ObservationType.ModifierCd();
			modifierCd.setValue(rowSet.getString("obs_modifier_cd"));
			modifierCd.setName(rowSet.getString("modifier_name"));
			observation.setModifierCd(modifierCd);

			Date startDate = rowSet.getTimestamp("obs_start_date");

			if (startDate != null) {
				observation.setStartDate(dtoFactory
						.getXMLGregorianCalendar(startDate.getTime()));
			}

			ObservationType.ObserverCd observerCd = new ObservationType.ObserverCd();
			observerCd.setValue(rowSet.getString("obs_provider_id"));
			String providerName = rowSet.getString("provider_name");
			observerCd.setSoruce((providerName != null) ? providerName : "");
			observation.setObserverCd(observerCd);
			if (obsFactDetailFlag) {
				Date endDate = rowSet.getTimestamp("obs_end_date");

				if (endDate != null) {
					observation.setEndDate(dtoFactory
							.getXMLGregorianCalendar(endDate.getTime()));
				}

				observation.setValuetypeCd(rowSet.getString("obs_valtype_cd"));
				observation.setTvalChar(rowSet.getString("obs_tval_char"));

				ObservationType.NvalNum nvalNum = new ObservationType.NvalNum();
				nvalNum.setValue(rowSet.getBigDecimal("obs_nval_num"));
				observation.setNvalNum(nvalNum);

				ObservationType.ValueflagCd valueFlagCd = new ObservationType.ValueflagCd();
				valueFlagCd.setValue(rowSet.getString("obs_valueflag_cd"));
				observation.setValueflagCd(valueFlagCd);

				observation.setQuantityNum(rowSet
						.getBigDecimal("obs_quantity_num"));

				observation.setUnitsCd(rowSet.getString("obs_units_cd"));

				if (rowSet.getString("obs_location_cd") != null) {
					ObservationType.LocationCd locationCd = new ObservationType.LocationCd();
					locationCd.setValue(rowSet.getString("obs_location_cd"));
					String locationName = rowSet.getString("location_name");
					locationCd.setName((locationName != null) ? locationName
							: "");
					observation.setLocationCd(locationCd);
				}

				observation.setConfidenceNum(rowSet
						.getBigDecimal("obs_confidence_num"));

			}

			if (obsFactBlobFlag) {
				Clob observationClob = rowSet.getClob("obs_observation_blob");
				if (observationClob != null) {
					try {
						BlobType blobType = new BlobType();
						blobType.getContent().add(
								JDBCUtil.getClobString(observationClob));
						observation.setObservationBlob(blobType);
					} catch (IOException ioEx) {
						ioEx.printStackTrace();
						throw ioEx;
					}
				}
			}

			if (obsFactStatusFlag) {
				if (rowSet.getTimestamp("obs_update_date") != null) {
					observation.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_update_date").getTime()));
				}

				if (rowSet.getDate("obs_download_date") != null) {
					observation.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_download_date").getTime()));
				}

				if (rowSet.getDate("obs_import_date") != null) {
					observation.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_import_date").getTime()));
				}

				observation.setSourcesystemCd(rowSet
						.getString("obs_sourcesystem_cd"));
				observation.setUploadId(rowSet.getString("obs_upload_id"));
			}

			return observation;
		}
	}

	/*
	 * Inner class to build patient in table PDO format
	 */
	public static class PatientBuilder {
		boolean patientDetailFlag = false;
		boolean patientBlobFlag = false;
		boolean patientStatusFlag = false;

		public PatientBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.patientDetailFlag = detailFlag;
			this.patientBlobFlag = blobFlag;
			this.patientStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build patient set
		 * 
		 * @param rowSet
		 * @param source
		 * @return PatientSet.Patient
		 * @throws SQLException
		 * @throws IOException
		 */
		public PatientType buildPatientSet(ResultSet rowSet, String source)
				throws SQLException, IOException {
			PatientType patientDimensionType = new PatientType();
			PatientIdType patientIdType = new PatientIdType();
			patientIdType.setSource(source);
			patientIdType.setValue(rowSet.getString("patient_patient_num"));
			patientDimensionType.setPatientId(patientIdType);

			if (patientDetailFlag) {

				ParamType vitalParamType = new ParamType();
				vitalParamType.setName(rowSet.getString("vital_status_name"));
				vitalParamType.setValue(rowSet
						.getString("patient_vital_status_cd"));
				vitalParamType.setColumn("vital_status_cd");
				patientDimensionType.getParam().add(vitalParamType);

				Date birthDate = rowSet.getTimestamp("patient_birth_date");

				if (birthDate != null) {
					ParamType birthParamType = new ParamType();
					birthParamType.setColumn("birth_date");
					birthParamType.setName("birth_date");
					birthParamType.setValue(dtoFactory.getXMLGregorianCalendar(
							birthDate.getTime()).toString());
					patientDimensionType.getParam().add(birthParamType);
				}

				Date deathDate = rowSet.getTimestamp("patient_death_date");

				if (deathDate != null) {
					ParamType deathParamType = new ParamType();
					deathParamType.setColumn("death_date");
					deathParamType.setName("death_date");
					deathParamType.setValue(dtoFactory.getXMLGregorianCalendar(
							deathDate.getTime()).toString());
					patientDimensionType.getParam().add(deathParamType);
				}

				ParamType sexCdParamType = new ParamType();
				sexCdParamType.setValue(rowSet.getString("patient_sex_cd"));
				sexCdParamType.setName(rowSet.getString("sex_name"));
				sexCdParamType.setColumn("sex_cd");
				patientDimensionType.getParam().add(sexCdParamType);

				ParamType ageParamType = new ParamType();
				ageParamType.setValue(rowSet
						.getString("patient_age_in_years_num"));
				ageParamType.setColumn("age_in_years_num");
				ageParamType.setName("age_in_years_num");
				patientDimensionType.getParam().add(ageParamType);

				ParamType languageParamType = new ParamType();
				languageParamType.setValue(rowSet
						.getString("patient_language_cd"));
				languageParamType.setName(rowSet.getString("language_name"));
				languageParamType.setColumn("language_cd");
				patientDimensionType.getParam().add(languageParamType);

				ParamType raceParamType = new ParamType();
				raceParamType.setValue(rowSet.getString("patient_race_cd"));
				raceParamType.setName(rowSet.getString("race_name"));
				raceParamType.setColumn("race_cd");
				patientDimensionType.getParam().add(raceParamType);

				ParamType religionParamType = new ParamType();
				religionParamType.setValue(rowSet
						.getString("patient_religion_cd"));
				religionParamType.setName(rowSet.getString("religion_name"));
				religionParamType.setColumn("religion_cd");
				patientDimensionType.getParam().add(religionParamType);

				ParamType maritalParamType = new ParamType();
				maritalParamType.setValue(rowSet
						.getString("patient_marital_status_cd"));
				maritalParamType.setName(rowSet
						.getString("marital_status_name"));
				maritalParamType.setColumn("marital_status_cd");
				patientDimensionType.getParam().add(maritalParamType);

				ParamType stateParamType = new ParamType();
				stateParamType.setName("statecityzip_path_char");
				stateParamType.setColumn("statecityzip_path_char");
				stateParamType.setValue(rowSet.getString("patient_zip_cd"));
				patientDimensionType.getParam().add(stateParamType);

			}

			if (patientBlobFlag) {
				if (rowSet.getClob("patient_patient_blob") != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobString(rowSet
									.getClob("patient_patient_blob")));
				}
			}

			if (patientStatusFlag) {
				if (rowSet.getTimestamp("patient_update_date") != null) {
					patientDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_update_date").getTime()));
				}

				if (rowSet.getTimestamp("patient_download_date") != null) {
					patientDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_download_date").getTime()));
				}

				if (rowSet.getTimestamp("patient_import_date") != null) {
					patientDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_import_date").getTime()));
				}

				patientDimensionType.setSourcesystemCd(rowSet
						.getString("patient_sourcesystem_cd"));
				patientDimensionType.setUploadId(rowSet
						.getString("patient_upload_id"));
			}

			return patientDimensionType;
		}
	}

	/*
	 * Inner class to build observer section in table PDO format
	 */
	public static class ProviderBuilder {
		boolean providerDetailFlag = false;
		boolean providerBlobFlag = false;
		boolean providerStatusFlag = false;

		public ProviderBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.providerDetailFlag = detailFlag;
			this.providerBlobFlag = blobFlag;
			this.providerStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build observer set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ObserverSet.Observer
		 * @throws SQLException
		 * @throws IOException
		 */
		public ObserverType buildObserverSet(ResultSet rowSet)
				throws IOException, SQLException {
			ObserverType providerDimensionType = new ObserverType();
			providerDimensionType.setObserverCd(rowSet
					.getString("provider_provider_id"));
			providerDimensionType.setObserverPath(rowSet
					.getString("provider_provider_path"));

			if (providerDetailFlag) {
				providerDimensionType.setNameChar(rowSet
						.getString("provider_name_char"));
			}

			if (providerBlobFlag) {
				Clob providerClob = rowSet.getClob("provider_provider_blob");

				if (providerClob != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobString(providerClob));
					providerDimensionType.setObserverBlob(blobType);
				}
			}

			if (providerStatusFlag) {
				if (rowSet.getTimestamp("provider_update_date") != null) {
					providerDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_update_date").getTime()));
				}

				if (rowSet.getTimestamp("provider_download_date") != null) {
					providerDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_download_date").getTime()));
				}

				if (rowSet.getTimestamp("provider_import_date") != null) {
					providerDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_import_date").getTime()));
				}

				providerDimensionType.setSourcesystemCd(rowSet
						.getString("provider_sourcesystem_cd"));
				providerDimensionType.setUploadId(rowSet
						.getString("provider_upload_id"));

			}

			return providerDimensionType;
		}
	}

	/*
	 * Inner class to build concept section in table PDO format
	 */
	public static class ConceptBuilder {
		boolean conceptDetailFlag = false;
		boolean conceptBlobFlag = false;
		boolean conceptStatusFlag = false;

		public ConceptBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.conceptDetailFlag = detailFlag;
			this.conceptBlobFlag = blobFlag;
			this.conceptStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build concept set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ConceptSet.Concept
		 * @throws SQLException
		 * @throws IOException
		 */
		public ConceptType buildConceptSet(ResultSet rowSet)
				throws SQLException, IOException {
			ConceptType conceptDimensionType = new ConceptType();

			conceptDimensionType.setConceptCd(rowSet
					.getString("concept_concept_cd"));

			if (conceptDetailFlag) {
				conceptDimensionType.setConceptPath(rowSet
						.getString("concept_concept_path"));
				conceptDimensionType.setNameChar(rowSet
						.getString("concept_name_char"));
			}

			if (conceptBlobFlag) {
				Clob conceptClob = rowSet.getClob("concept_concept_blob");

				if (conceptClob != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobString(conceptClob));
					conceptDimensionType.setConceptBlob(blobType);
				}
			}

			if (conceptStatusFlag) {
				if (rowSet.getTimestamp("concept_update_date") != null) {
					conceptDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_update_date").getTime()));
				}

				if (rowSet.getTimestamp("concept_download_date") != null) {
					conceptDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_download_date").getTime()));
				}

				if (rowSet.getTimestamp("concept_import_date") != null) {
					conceptDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_import_date").getTime()));
				}

				conceptDimensionType.setSourcesystemCd(rowSet
						.getString("concept_sourcesystem_cd"));

				conceptDimensionType.setUploadId(rowSet
						.getString("concept_upload_id"));
			}

			return conceptDimensionType;
		}
	}

	/*
	 * Inner class to build event section in table PDO format
	 */
	public static class EventBuilder {
		boolean eventDetailFlag = false;
		boolean eventBlobFlag = false;
		boolean eventStatusFlag = false;

		public EventBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.eventDetailFlag = detailFlag;
			this.eventBlobFlag = blobFlag;
			this.eventStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build event set
		 * 
		 * @param rowSet
		 * @param source
		 * @return EventSet.Event
		 * @throws SQLException
		 * @throws IOException
		 */
		public EventType buildEventSet(ResultSet rowSet, String source)
				throws SQLException, IOException {
			EventType visitDimensionType = new EventType();

			PatientIdType patientIdType = new PatientIdType();
			patientIdType.setValue(rowSet.getString("visit_patient_num"));
			patientIdType.setSource(source);
			visitDimensionType.setPatientId(patientIdType);

			EventType.EventId eventId = new EventType.EventId();
			eventId.setValue(rowSet.getString("visit_encounter_num"));
			eventId.setSource(source);
			visitDimensionType.setEventId(eventId);

			if (eventDetailFlag) {
				ParamType inoutParamType = new ParamType();
				inoutParamType.setValue(rowSet.getString("visit_inout_cd"));
				inoutParamType.setName(rowSet.getString("inout_name"));
				inoutParamType.setColumn("inout_cd");
				visitDimensionType.getParam().add(inoutParamType);

				ParamType locationParamType = new ParamType();
				locationParamType.setValue(rowSet
						.getString("visit_location_cd"));
				locationParamType.setName(rowSet.getString("location_name"));
				locationParamType.setColumn("location_cd");
				visitDimensionType.getParam().add(locationParamType);

				ParamType siteParamType = new ParamType();
				siteParamType.setName(rowSet.getString("visit_location_path"));
				siteParamType.setColumn("location_path");
				// locationParamType.setColumn("site_cd");
				siteParamType.setValue(rowSet.getString("visit_location_path"));
				visitDimensionType.getParam().add(siteParamType);

				ParamType activeStatusParamType = new ParamType();
				activeStatusParamType.setValue(rowSet
						.getString("visit_active_status_cd"));
				activeStatusParamType.setName(rowSet
						.getString("active_status_name"));
				activeStatusParamType.setColumn("active_status_cd");
				visitDimensionType.getParam().add(activeStatusParamType);

				Date startDate = rowSet.getTimestamp("visit_start_date");

				if (startDate != null) {
					visitDimensionType.setStartDate(dtoFactory
							.getXMLGregorianCalendar(startDate.getTime()));
				}

				Date endDate = rowSet.getTimestamp("visit_end_date");

				if (endDate != null) {
					visitDimensionType.setEndDate(dtoFactory
							.getXMLGregorianCalendar(endDate.getTime()));
				}
			}

			if (eventBlobFlag) {
				Clob visitClob = rowSet.getClob("visit_visit_blob");

				if (visitClob != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobString(rowSet
									.getClob("visit_visit_blob")));
					visitDimensionType.setEventBlob(blobType);
				}
			}

			if (eventStatusFlag) {
				if (rowSet.getTimestamp("visit_update_date") != null) {
					visitDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_update_date").getTime()));
				}

				if (rowSet.getTimestamp("visit_download_date") != null) {
					visitDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_download_date").getTime()));
				}

				if (rowSet.getTimestamp("visit_import_date") != null) {
					visitDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_import_date").getTime()));
				}

				visitDimensionType.setSourcesystemCd(rowSet
						.getString("visit_sourcesystem_cd"));

				visitDimensionType.setUploadId(rowSet
						.getString("visit_upload_id"));
			}

			return visitDimensionType;
		}
	}

	/*
	 * Inner class to build pid section in table PDO format
	 */
	public static class PidBuilder {
		boolean pmDetailFlag = false;
		boolean pmBlobFlag = false;
		boolean pmStatusFlag = false;

		public PidBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.pmDetailFlag = detailFlag;
			this.pmBlobFlag = blobFlag;
			this.pmStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build concept set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ConceptSet.Concept
		 * @throws SQLException
		 * @throws IOException
		 */
		public PidType buildPidSet(ResultSet rowSet) throws SQLException,
				IOException {
			PidType.PatientMapId patientMapType = new PidType.PatientMapId();
			patientMapType.setValue(rowSet.getString("pm_patient_ide"));
			patientMapType.setSource(rowSet.getString("pm_patient_ide_source"));

			PatientId patientId = new PatientId();
			patientId.setValue(rowSet.getString("pm_patient_num"));

			PidType pidType = new PidType();
			pidType.setPatientId(patientId);
			pidType.getPatientMapId().add(patientMapType);
			// patientMapType.setValue(rowSet.getString("pm_patient_num"));

			if (pmDetailFlag) {
				patientMapType.setStatus(rowSet
						.getString("pm_patient_ide_status"));
			}

			if (pmBlobFlag) {
				; // no blob field in the mapping table
			}

			if (pmStatusFlag) {
				if (rowSet.getTimestamp("pm_update_date") != null) {
					patientMapType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"pm_update_date").getTime()));
				}

				if (rowSet.getTimestamp("pm_download_date") != null) {
					patientMapType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"pm_download_date").getTime()));
				}

				if (rowSet.getTimestamp("pm_import_date") != null) {
					patientMapType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"pm_import_date").getTime()));
				}

				patientMapType.setSourcesystemCd(rowSet
						.getString("pm_sourcesystem_cd"));
				patientMapType.setUploadId(rowSet.getString("pm_upload_id"));

			}

			return pidType;
		}
	}

	/*
	 * Inner class to build pid section in table PDO format
	 */
	public static class EidBuilder {
		boolean pmDetailFlag = false;
		boolean pmBlobFlag = false;
		boolean pmStatusFlag = false;

		public EidBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.pmDetailFlag = detailFlag;
			this.pmBlobFlag = blobFlag;
			this.pmStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build concept set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ConceptSet.Concept
		 * @throws SQLException
		 * @throws IOException
		 */
		public EidType buildEidSet(ResultSet rowSet) throws SQLException,
				IOException {
			EidType.EventMapId eventMapType = new EidType.EventMapId();
			eventMapType.setValue(rowSet.getString("em_encounter_ide"));
			eventMapType.setSource(rowSet.getString("em_encounter_ide_source"));
			eventMapType.setPatientId(rowSet.getString("em_patient_ide"));
			eventMapType.setPatientIdSource(rowSet
					.getString("em_patient_ide_source"));

			EidType.EventId eventId = new EidType.EventId();
			eventId.setValue(rowSet.getString("em_encounter_num"));

			EidType eidType = new EidType();
			eidType.setEventId(eventId);
			eidType.getEventMapId().add(eventMapType);
			// patientMapType.setValue(rowSet.getString("pm_patient_num"));

			if (pmDetailFlag) {
				eventMapType.setStatus(rowSet
						.getString("em_encounter_ide_status"));
				eventId.setStatus(eventMapType.getStatus());
			}

			if (pmBlobFlag) {
				; // no blob field in the mapping table
			}

			if (pmStatusFlag) {
				if (rowSet.getTimestamp("em_update_date") != null) {
					eventMapType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"em_update_date").getTime()));
					eventId.setUpdateDate(eventMapType.getUpdateDate());
				}

				if (rowSet.getTimestamp("em_download_date") != null) {
					eventMapType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"em_download_date").getTime()));
					eventId.setDownloadDate(eventMapType.getDownloadDate());
				}

				if (rowSet.getTimestamp("em_import_date") != null) {
					eventMapType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"em_import_date").getTime()));
					eventId.setImportDate(eventMapType.getImportDate());
				}

				eventMapType.setSourcesystemCd(rowSet
						.getString("em_sourcesystem_cd"));
				eventId.setSourcesystemCd(eventMapType.getSourcesystemCd());

				eventMapType.setUploadId(rowSet.getString("em_upload_id"));
				eventId.setUploadId(eventMapType.getUploadId());
			}

			// 

			return eidType;
		}
	}
}
