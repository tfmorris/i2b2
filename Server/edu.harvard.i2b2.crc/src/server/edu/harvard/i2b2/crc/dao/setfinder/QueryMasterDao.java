/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.IntegerStringUserType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.CustomType;

import java.util.Date;
import java.util.List;


/**
 * Class to manager persistance operation of
 * QtQueryMaster
 * $Id: QueryMasterDao.java,v 1.10 2007/08/31 14:44:26 rk903 Exp $
 * @author rkuttan
 * @see QtQueryMaster
 */
public class QueryMasterDao extends CRCDAO {
    /**
     * Function to create query master
     * By default sets delete flag to false
     * @param queryMaster
     * @return query master id
     */
    public String createQueryMaster(QtQueryMaster queryMaster) {
        Session session = getSession();
        queryMaster.setDeleteFlag(QtQueryMaster.DELETE_OFF_FLAG);
        session.save(queryMaster);

        return queryMaster.getQueryMasterId();
    }

    /**
     * Returns list of query master by user id
     * @param userId
     * @return List<QtQueryMaster>
     */
    @SuppressWarnings("unchecked")
    public List<QtQueryMaster> getQueryMasterByUserId(String userId,int fetchSize) {
        Session session = getSession();
        Query query = session.createQuery(
                "select new QtQueryMaster(qm.queryMasterId, qm.name, qm.userId, qm.groupId, qm.createDate) from QtQueryMaster qm where qm.userId = :userId order by qm.createDate desc");
        query.setString("userId", userId);
        if (fetchSize>0) {
        	query.setMaxResults(fetchSize);
        }
        List<QtQueryMaster> queryMasterList = query.list();

        return queryMasterList;
    }

    /**
     * Returns list of query master by group id
     * @param groupId
     * @return List<QtQueryMaster>
     */
    @SuppressWarnings("unchecked")
    public List<QtQueryMaster> getQueryMasterByGroupId(String groupId,int fetchSize) {
        Session session = getSession();
        Query query = session.createQuery(
                "select new QtQueryMaster(qm.queryMasterId, qm.name, qm.userId, qm.groupId, qm.createDate) from QtQueryMaster qm where qm.groupId = :groupId order by qm.createDate desc ");
        query.setString("groupId", groupId);
        
        if (fetchSize>0) {
        	query.setMaxResults(fetchSize);
        }

        List<QtQueryMaster> queryMasterList = query.list();

        return queryMasterList;
    }

    /**
     * Find Query master by id
     * @param masterId
     * @return QtQueryMaster
     */
    public QtQueryMaster getQueryDefinition(String masterId) {
        Session session = getSession();
        Query query = session.createQuery(
                "select qm from QtQueryMaster qm where qm.queryMasterId = :masterId");
        query.setParameter("masterId", masterId,Hibernate.custom(IntegerStringUserType.class));

        QtQueryMaster queryMaster = (QtQueryMaster) query.uniqueResult();

        return queryMaster;
    }



    /**
     * Function to rename query master
     * @param masterId
     * @param queryNewName
     * @throws I2B2DAOException
     */
    public void renameQuery(String masterId, String queryNewName)
        throws I2B2DAOException {
        Session session = getSession();
        log.debug("Rename  masterId=" + masterId +
            " new query name" + queryNewName);

        Query query = session.createQuery(
                "select qm from QtQueryMaster qm where  qm.queryMasterId = :masterId");
        query.setParameter("masterId", masterId,Hibernate.custom(IntegerStringUserType.class));

        QtQueryMaster queryMaster = (QtQueryMaster) query.uniqueResult();

        if (queryMaster == null) {
            throw new I2B2DAOException("Query with master id " + masterId +
                " not found");
        }

        //query = session.createQuery(
        //        "select qm from QtQueryMaster qm where qm.userId = :userId and qm.name = :queryNewName");
        //query.setString("userId", userId);
        //query.setString("queryNewName", queryNewName);

        //List queryMasterList = query.list();

        //if ((queryMasterList != null) && (queryMasterList.size() > 0)) {
        //    throw new I2B2DAOException("Query name '" + queryNewName +
        //        "' already taken");
        //} else {
        queryMaster.setName(queryNewName);
        session.update(queryMaster);
        //}
    }

    /**
     * Function to delete query using user and master id
     * This function will not delete permanently, it will set
     * delete flag field in query master, query instance and result instance
     * to true
     * @param masterId
     * @throws I2B2DAOException
     */
    @SuppressWarnings("unchecked")
    public void deleteQuery(String masterId)
        throws I2B2DAOException {
        log.error("Delete query for master id=" +
            masterId);

        Session session = getSession();
        Query query = null;
        query = session.createQuery(
                "select qm from QtQueryMaster qm where  qm.queryMasterId = :masterId");
        query.setParameter("masterId", masterId,Hibernate.custom(IntegerStringUserType.class));


        QtQueryMaster queryMaster = (QtQueryMaster) query.uniqueResult();

        if (queryMaster == null) {
            throw new I2B2DAOException("Query not found with masterid =[" +
                masterId + "]" );
        }

        //TODO Upgrade to Hibernate 3.2.1 to support filter with hsql for update statement
        query = session.createQuery(
                "select qi from QtQueryInstance qi where  qi.qtQueryMaster = :masterId");
        query.setParameter("masterId", masterId,Hibernate.custom(IntegerStringUserType.class));

        List<QtQueryInstance> queryInstanceList = query.list();

        for (QtQueryInstance queryInstance : queryInstanceList) {
            query = session.createQuery(
                    "update  QtQueryResultInstance  set deleteFlag = :deleteFlag where  qtQueryInstance =  :queryInstanceId");
            query.setString("deleteFlag", QtQueryMaster.DELETE_ON_FLAG);
            //query.setInteger("queryInstanceId",
            //    queryInstance.getQueryInstanceId());
            query.setParameter("queryInstanceId", queryInstance.getQueryInstanceId(), 
            		  Hibernate.custom(IntegerStringUserType.class));
            query.executeUpdate();
        }

        query = session.createQuery(
                "update QtQueryInstance set  deleteFlag = :deleteFlag where qtQueryMaster = :masterId)");
        query.setString("deleteFlag", QtQueryMaster.DELETE_ON_FLAG);
        //query.setInteger("masterId", masterId);
        query.setParameter("masterId", masterId,Hibernate.custom(IntegerStringUserType.class));

        @SuppressWarnings("unused")
        int updatedInstanceCount = query.executeUpdate();

        queryMaster.setDeleteFlag(QtQueryMaster.DELETE_ON_FLAG);
        queryMaster.setDeleteDate(new Date(System.currentTimeMillis()));
        session.update(queryMaster);

        /*
        //:TODO Use this block which will do same operation without iteration each query instance.
        //:TODO This block will work only in Hibernate 3.2.1
        query = session.createQuery( "update  QtQueryResultInstance  set deleteFlag = :deleteFlag where  qtQueryInstance in ( " +
        " select qi from QtQueryInstance qi where qi.qtQueryMaster = :masterId)");
        query.setString("deleteFlag", QtQueryMaster.DELETE_ON_FLAG);
        query.setInteger("masterId", masterId);
        //int updatedResultInstanceCount = query.executeUpdate();
        
        query = session.createQuery( "update QtQueryInstance set deleteFlag = :deleteFlag where qtQueryMaster  in ( from QtQueryMaster where qtQueryMaster= :masterId)");
        query.setString("deleteFlag", QtQueryMaster.DELETE_ON_FLAG);
        query.setInteger("masterId", masterId);
        int updatedInstanceCount = query.executeUpdate();
        
        
        query = session.createSQLQuery("update qt_query_result_instance set " +
                        " delete_flag=:deleteFlag where query_instance_id in (select query_instance_id from qt_query_instance where query_master_id=:masterId )");
        query.setString("deleteFlag", QtQueryMaster.DELETE_ON_FLAG);
        query.setInteger("masterId", masterId);
        query.executeUpdate();
        */
    }
}
