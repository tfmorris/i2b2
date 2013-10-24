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

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.IntegerStringUserType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Class to handle persistance operation of Query instance
 * i.e. each run of query is called query instance
 * $Id: QueryInstanceDao.java,v 1.5 2007/08/31 14:44:26 rk903 Exp $
 * @author rkuttan
 * @see QtQueryInstance
 */
public class QueryInstanceDao extends CRCDAO {
    /**
     * Function to create query instance
     * @param queryMasterId
     * @param userId
     * @param groupId
     * @param batchMode
     * @param statusId
     * @return query instance id
     */
    public String createQueryInstance(String queryMasterId, String userId,
        String groupId, String batchMode, int statusId) {
        QtQueryInstance queryInstance = new QtQueryInstance();
        queryInstance.setUserId(userId);
        queryInstance.setGroupId(groupId);
        queryInstance.setBatchMode(batchMode);
        queryInstance.setDeleteFlag("N");

        QtQueryMaster queryMaster = new QtQueryMaster();
        queryMaster.setQueryMasterId(queryMasterId);
        queryInstance.setQtQueryMaster(queryMaster);

        QtQueryStatusType statusType = new QtQueryStatusType();
        statusType.setStatusTypeId(statusId);

        Date startDate = Calendar.getInstance().getTime();
        queryInstance.setStartDate(startDate);

        Session session = getSession();
        queryInstance.setQtQueryStatusType(statusType);
        session.persist(queryInstance);

        return queryInstance.getQueryInstanceId();
    }

    /**
     * Returns list of query instance for
     * the given master id
     * @param queryMasterId
     * @return  List<QtQueryInstance>
     */
    @SuppressWarnings("unchecked")
    public List<QtQueryInstance> getQueryInstanceByMasterId(
        String queryMasterId) {
        Session session = getSession();
        Query query = session.createQuery(
                "select qi  from QtQueryInstance qi where qi.qtQueryMaster = :queryMasterId");
        query.setParameter("queryMasterId", queryMasterId,Hibernate.custom(IntegerStringUserType.class));

        List<QtQueryInstance> queryInstanceList = query.list();

        return queryInstanceList;
    }

    /**
     * Find query instance by id
     * @param queryInstanceId
     * @return QtQueryInstance
     */
    public QtQueryInstance getQueryInstanceByInstanceId(
        String queryInstanceId) {
        Session session = getSession();
        Query query = session.createQuery(
                "select qi  from QtQueryInstance qi where qi.queryInstanceId = :queryInstanceId");
        //query.setInteger("queryInstanceId", queryInstanceId);
        query.setParameter("queryInstanceId", queryInstanceId,Hibernate.custom(IntegerStringUserType.class));

        QtQueryInstance queryInstance = (QtQueryInstance) query.uniqueResult();

        return queryInstance;
    }

    /**
     * Return list of query result instance by query instance id
     * @param queryInstanceId
     * @return  List<QtQueryResultInstance>
     */
    @SuppressWarnings("unchecked")
    public List<QtQueryResultInstance> getResultInstanceList(
        String queryInstanceId) {
        Session session = getSession();
        Query query = session.createQuery(
                "select qri  from QtQueryResultInstance qri where qri.qtQueryInstance = :queryInstanceId ");
        //query.setInteger("queryInstanceId", queryInstanceId);
        query.setParameter("queryInstanceId", queryInstanceId,Hibernate.custom(IntegerStringUserType.class));
        List<QtQueryResultInstance> queryResultInstanceList = query.list();

        return queryResultInstanceList;
    }

    /**
     * Update query instance
     * @param queryInstance
     * @return QtQueryInstance
     */
    public QtQueryInstance update(QtQueryInstance queryInstance) {
        Session session = getSession();
        session.update(queryInstance);
        session.flush();
        session.refresh(queryInstance);

        return queryInstance;
    }
}
