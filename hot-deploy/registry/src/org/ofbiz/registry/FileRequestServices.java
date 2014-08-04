package org.ofbiz.registry;



//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVFormat.CSVFormatBuilder;
//import org.apache.commons.csv.CSVRecord;
//import org.ofbiz.base.util.Debug;
//import org.ofbiz.base.util.UtilDateTime;
//import org.ofbiz.base.util.UtilFormatOut;
//import org.ofbiz.base.util.UtilGenerics;
//import org.ofbiz.base.util.UtilMisc;
//import org.ofbiz.base.util.UtilNumber;
//import org.ofbiz.base.util.UtilProperties;
//import org.ofbiz.base.util.UtilValidate;
//import org.ofbiz.entity.Delegator;
//import org.ofbiz.entity.GenericEntityException;
//import org.ofbiz.entity.GenericValue;
//import org.ofbiz.entity.condition.EntityCondition;
//import org.ofbiz.entity.condition.EntityExpr;
//import org.ofbiz.entity.condition.EntityOperator;
//import org.ofbiz.entity.util.EntityFindOptions;
//import org.ofbiz.entity.util.EntityUtil;
//import org.ofbiz.order.order.OrderReadHelper;
//import org.ofbiz.product.product.ProductWorker;
//import org.ofbiz.service.DispatchContext;
//import org.ofbiz.service.GenericServiceException;
//import org.ofbiz.service.LocalDispatcher;


public class FileRequestServices {
//public static Map createFileRequest(DispatchContext dctx, Map context) {
//       Map resultMap = ServiceUtil.returnSuccess();

       //this is how you fetch the values from the request
//       String registryFileId = (String) context.get("registryFileId");

//       try {
//              //delegator.getNextSeqId(String EntityName) for auto-increment id
//              Map personValue = UtilMisc.toMap("id", delegator.getNextSeqId("Person"),
//                     "firstName", firstName,
//                     "lastName", lastName,
//                     "gender", gender,
//                     "email", email);
//      
//             GenericValue personGV = delegator.makeValue("Person", personValue);
//             personGV.create();    
//       } catch (GenericEntityException e) {
//            return ServiceUtil.returnError("Failed. " +e.getMessage());
//       }
//       return resultMap;
//	return null;
//}
}