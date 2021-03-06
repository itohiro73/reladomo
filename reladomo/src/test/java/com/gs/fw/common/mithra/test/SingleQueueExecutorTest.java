/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.gs.fw.common.mithra.test;

import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.test.domain.*;
import org.slf4j.Logger;
import com.gs.fw.common.mithra.util.ExecutorErrorHandler;
import com.gs.fw.common.mithra.util.SingleQueueExecutor;

import java.sql.Timestamp;
import java.util.concurrent.ThreadPoolExecutor;



public class SingleQueueExecutorTest extends MithraTestAbstract
{
    private boolean errorCaught = false;

    public Class[] getRestrictedClassList()
    {
        return new Class[]
        {
            ParaBalance.class,
            ParaPosition.class,
        };
    }

    public void testSingleQueueExecutorSingleThread() throws Exception
    {
        Timestamp businessDate = new Timestamp(timestampFormat.parse("2011-09-30 23:59:00").getTime());
        SingleQueueExecutor executor = new SingleQueueExecutor(1, ParaPositionFinder.accountNumber().ascendingOrderBy(), 10,
                ParaPositionFinder.getFinderInstance(), 0);

        executor.setUseBulkInsert();

        Operation op = ParaPositionFinder.productIdentifier().eq("TSWAP:1000000001").
                       and(ParaPositionFinder.accountNumber().eq("76095661")).
                       and(ParaPositionFinder.accountSubtype().eq("01")).
                       and(ParaPositionFinder.businessDate().eq(businessDate));
        ParaPosition paraPositionDB = ParaPositionFinder.findOne(op);

        ParaPosition paraPosition = new ParaPosition(InfinityTimestamp.getParaInfinity());
        paraPosition.setProductIdentifier("TSWAP:1000000001");
        paraPosition.setUpdated(new Timestamp(timestampFormat.parse("2011-12-06 14:00:00.0").getTime()));
        paraPosition.setAccountNumber("76095661");
        paraPosition.setAccountSubtype("01");
        paraPosition.setQtdOpenCost(3);
        paraPosition.setYtdOpenCost(30);
        paraPosition.setQtdUnrealized(9);// changed value
        paraPosition.setYtdUnrealized(10);
        paraPosition.setQtdRealized(10);
        paraPosition.setYtdRealized(10);
        paraPosition.setQtdTrading(10);
        paraPosition.setYtdTrading(10);
        paraPosition.setBusinessDate(businessDate);

        executor.addForUpdate(paraPositionDB,paraPosition);
        executor.waitUntilFinished();
    }

    public void testSingleQueueExecutorErrorHandler()
    {
        Timestamp businessDate = new Timestamp(System.currentTimeMillis());
        SingleQueueExecutor executor = new SingleQueueExecutor(3, ParaBalanceFinder.balanceId().ascendingOrderBy(), 10,
                ParaBalanceFinder.getFinderInstance(), 12);
        executor.setErrorHandler(new TestErrorHandler());
        for(int i=0;i<2;i++)
        {
            ParaBalance bal = new ParaBalance(businessDate, InfinityTimestamp.getParaInfinity());
            bal.setBalanceId(5000);
            bal.setAcmapCode("A");
            executor.addForInsert(bal);
        }
        executor.waitUntilFinished();
        assertTrue(errorCaught);
    }


    private class TestErrorHandler implements ExecutorErrorHandler
    {

        public void handle(Throwable t, Logger logger, SingleQueueExecutor sqe, ThreadPoolExecutor executor, SingleQueueExecutor.CallableTask task)
        {
            errorCaught = true;
        }

    }
}
