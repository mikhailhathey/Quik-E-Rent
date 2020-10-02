package project.quikERent.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import project.quikERent.models.PowerToolModel;
import project.quikERent.models.RentedPowerToolModel;
import project.quikERent.models.ConfirmationPowerToolModel;
import project.quikERent.models.ConfirmationType;
import project.quikERent.models.SuggestPowerToolModel;
import project.quikERent.models.UserModel;


public class DataStoreUtils {

    public static List<PowerToolModel> readPowerTools(Object list) {
        final List<Object> listOfPowerTools = (List) list;
        List<PowerToolModel> powerTools = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(listOfPowerTools)) {
            for (Object field : listOfPowerTools) {
                if (field == null) {
                    continue;
                }
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long id = (Long) fields.get("id");
                String brand = (String) fields.get("brand");
                String powerToolName = (String) fields.get("powerToolName");
                Integer year = ((Long) fields.get("year")).intValue();
                powerTools.add(new PowerToolModel(id, brand, powerToolName, year));
            }
        }
        return powerTools;
    }

    public static List<RentedPowerToolModel> readRentedPowerTools(Object list) {
        //final List<Object> listOfRentedPowerTools = (List) list;
        final List<Object> listOfRentedPowerTools = Arrays.asList((((HashMap) list).values().toArray()));
        List<RentedPowerToolModel> powerTools = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(listOfRentedPowerTools)) {
            for (Object field : listOfRentedPowerTools) {
                if (field == null) {
                    continue;
                }
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long powerToolId = (Long) fields.get("powerToolId");
                String userId = (String) fields.get("userId");
                Date datetime = new Date((Long) ((HashMap) fields.get("rentDate")).get("time"));
                powerTools.add(new RentedPowerToolModel(powerToolId, userId, datetime));
            }
        }
        return powerTools;
    }

    public static List<SuggestPowerToolModel> readRequest(Object list) {
        //final List<Object> listOfRentedPowerTools = (List) list;
        final List<Object> listOfRentedPowerTools = Arrays.asList((((HashMap) list).values().toArray()));
        List<SuggestPowerToolModel> requests = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(listOfRentedPowerTools)) {
            for (Object field : listOfRentedPowerTools) {
                if (field == null) {
                    continue;
                }
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long id = (Long) fields.get("id");
                String brand = (String) fields.get("brand");
                Integer year = ((Long) fields.get("year")).intValue();
                String powerToolName = (String) fields.get("powerToolName");
                requests.add(new SuggestPowerToolModel(id, brand, powerToolName, year));
            }
        }
        return requests;
    }

    public static List<ConfirmationPowerToolModel> readConfirmations(Object list) {
        //final List<Object> listOfConfirmations = (List) list;
        final List<Object> listOfConfirmations = Arrays.asList((((HashMap) list).values().toArray()));
        List<ConfirmationPowerToolModel> confirmations = new ArrayList<>();
        Date now = new Date();
        if (CollectionUtils.isNotEmpty(listOfConfirmations)) {
            for (Object field : listOfConfirmations) {
                if (field == null) {
                    continue;
                }
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                Long powerToolId = (Long) fields.get("powerToolId");
                String userId = (String) fields.get("userId");
                ConfirmationType type = ConfirmationType.valueOf((String) fields.get("type"));
                Date datetime = new Date((Long) ((HashMap) fields.get("datetime")).get("time"));
                if (datetime.after(now))
                    confirmations.add(new ConfirmationPowerToolModel(powerToolId, userId, type, datetime));
            }
        }
        return confirmations;
    }

    public static List<UserModel> readUsers(Object list) {
        final List<Object> listOfUsers = Arrays.asList((((HashMap) list).values().toArray()));
        List<UserModel> users = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(listOfUsers)) {
            for (Object field : listOfUsers) {
                if (field == null) {
                    continue;
                }
                HashMap<String, Object> fields = (HashMap<String, Object>) field;
                String uId = (String) fields.get("uid");
                String email = (String) fields.get("email");
                String displayName = (String) fields.get("displayName");
                String phoneNumber = (String) fields.get("phoneNumber");
                users.add(new UserModel(uId, email, displayName, phoneNumber));
            }
        }
        return users;
    }
}
