package localsearch.applications.idpcndu_network.constraint;

import localsearch.model.*;

import java.util.*;

public class IdpcnduConstraint extends AbstractInvariant implements IConstraint {

    private final LocalSearchManager localSearchManager;
    private final VarIntLS[] variables;

    private int violations = 0;
    private final Map<Integer, Integer> valueToEntryTime = new HashMap<>();

    public IdpcnduConstraint(VarIntLS[] variables) {
        this.variables = variables;

        localSearchManager = variables[0].getLocalSearchManager();
        localSearchManager.post(this);

    }

    @Override
    public String name() {
        return "IdpcnduConstraint";
    }


    @Override
    public int violations() {
        // TODO Auto-generated method stub
        return violations;
    }

    /** x có violation bao nhiêu **/
    @Override
    public int violations(VarIntLS x) {
        return valueToEntryTime.containsKey(x.getValue()) ? valueToEntryTime.get(Integer.valueOf(x.getValue())) : 0;
    }

    @Override
    public VarIntLS[] getVariables() {
        return variables;
    }

    /** nếu thay x bằng val violation tăng giảm ntn **/
    @Override
    public int getAssignDelta(VarIntLS x, int val) {
        // nếu ko có x trong tập giá trị, hoặc giá trị của x == val
        if ((!Arrays.asList(variables).contains(x)) || x.getValue() == val) {
            return 0;
        }
        int newViolation = violations;

        int indexOfX = Arrays.asList(variables).indexOf(x);
        if(indexOfX >0 && indexOfX < variables.length - 1){
            int valueBeforeX = variables[indexOfX - 1].getValue();
            int valueAfterX = variables[indexOfX + 1].getValue();

            if(valueBeforeX == x.getOldValue() && valueAfterX == x.getOldValue()){
                if(valueToEntryTime.containsKey(x.getOldValue())){
                    newViolation++;
                }
            } else if ( valueBeforeX != x.getOldValue() && valueAfterX != x.getOldValue()){
                if(valueToEntryTime.get(x.getOldValue()) != 1){
                    newViolation--;
                }
            }

            if(valueBeforeX == val && valueAfterX == val){
                if(valueToEntryTime.get(val) != 1){
                    newViolation--;
                }
            } else if ( valueBeforeX != val && valueAfterX != val){
                if(valueToEntryTime.containsKey(val)){
                    newViolation++;
                }
            }
        } else if (indexOfX == 0) {
            int valueAfterX = variables[indexOfX + 1].getValue();
            if (x.getOldValue() != valueAfterX) {
                if(valueToEntryTime.get(x.getOldValue()) != 1){
                    newViolation--;
                }
            }

            if (val != valueAfterX) {
                if(valueToEntryTime.get(val) != 1){
                    newViolation--;
                }
            }
        } else {
            int valueBeforeX = variables[indexOfX - 1].getValue();
            if (x.getOldValue() != valueBeforeX) {
                if(valueToEntryTime.get(val) != 1){
                    newViolation--;
                }
            }

            if (val != valueBeforeX) {
                if(valueToEntryTime.containsKey(val)){
                    newViolation++;
                }
            }
        }

        return newViolation-violations;
    }

    /** nếu thay old bằng new thì violation tăng giảm ntn **/
    @Override
    public int getSwapDelta(VarIntLS oldVar, VarIntLS newVar) {
        // TODO Auto-generated method stub
        // nếu cả 2 đều ko có trong vảiable lít, trả về 0
        return getAssignDelta(oldVar, newVar.getValue());
    }

    /** thay đổi x bằng val trong runtime **/
    @Override
    public void propagateInt(VarIntLS x, int val) {
        if (!Arrays.asList(variables).contains(x) || x.getOldValue() == val) {
            return;
        }
        int indexOfX = Arrays.asList(variables).indexOf(x);
        if(indexOfX >0 && indexOfX < variables.length - 1){
            int valueBeforeX = variables[indexOfX - 1].getValue();
            int valueAfterX = variables[indexOfX + 1].getValue();

            if(valueBeforeX == x.getOldValue() && valueAfterX == x.getOldValue()){
                increaseEntry(x.getOldValue());
            } else if ( valueBeforeX != x.getOldValue() && valueAfterX != x.getOldValue()){
                decreaseEntry(x.getOldValue());
            }

            if(valueBeforeX == val && valueAfterX == val){
                decreaseEntry(val);
            } else if ( valueBeforeX != val && valueAfterX != val){
                increaseEntry(val);
            }
        } else if (indexOfX == 0) {
            int valueAfterX = variables[indexOfX + 1].getValue();
            if (x.getOldValue() != valueAfterX) {
                decreaseEntry(x.getOldValue());
            }

            if (val != valueAfterX) {
                increaseEntry(val);
            }
        } else {
            int valueBeforeX = variables[indexOfX - 1].getValue();
            if (x.getOldValue() != valueBeforeX) {
                decreaseEntry(x.getOldValue());
            }

            if (val != valueBeforeX) {
                increaseEntry(val);
            }
        }
    }

    @Override
    public void initPropagate() {
        violations = 0;
        valueToEntryTime.put(variables[0].getValue(), 1);
        for (int i = 1; i < variables.length; i++) {
            if (variables[i].getValue() != variables[i - 1].getValue()) {
                if (valueToEntryTime.containsKey(variables[i].getValue())) {
                    valueToEntryTime.put(variables[i].getValue(), valueToEntryTime.get(variables[i].getValue()) + 1);
                } else {
                    valueToEntryTime.put(variables[i].getValue(), 1);
                }
            }
        }
        for (int entry : valueToEntryTime.values()) {
            violations += entry - 1;
        }
    }

    //check xem chạy chính xác hay chưa
    @Override
    public boolean verify() {
        // TODO Auto-generated method stub
        // array lưu số lần xuất hiện giá trị của biến
        Map<Integer, Integer> valueToEntryTime = new HashMap<>();

        valueToEntryTime.put(variables[0].getValue(), 1);
        for (int i = 1; i < variables.length; i++) {
            if (variables[i].getValue() != variables[i - 1].getValue()) {
                if (valueToEntryTime.containsKey(variables[i].getValue())) {
                    valueToEntryTime.put(variables[i].getValue(), valueToEntryTime.get(variables[i].getValue()) + 1);
                } else {
                    valueToEntryTime.put(variables[i].getValue(), 1);
                }
            }
        }

        //so sánh array vừa tạo với array có sẵn, nếu có khác biệt thì tức là sai
        if (!this.valueToEntryTime.equals(valueToEntryTime)) {
            System.out.println(name() + "::verify failed");
            return false;
        }

        //số violation của dãy
        int violations = 0;

        for (int entry : valueToEntryTime.values()) {
            violations += entry - 1;
        }

        // so sánh violation vừa tính được với số có sẵn, nếu có khác biệt tức là sai
        if (violations != this.violations) {
            System.out.println(name() + "::verify failed, _violations = " + this.violations + " differs from violations = "
                    + violations + " by reputation");
        }
        return true;
    }

    @Override
    public LocalSearchManager getLocalSearchManager() {
        // TODO Auto-generated method stub
        return localSearchManager;
    }

    public boolean valueCauseViolation(int value){
        if(valueToEntryTime.containsKey(value)){
            return valueToEntryTime.get(Integer.valueOf(value)) > 1;
        } else {
            return true;
        }
    }

    private void increaseEntry(int entryValue){
        if(valueToEntryTime.containsKey(entryValue)){
            valueToEntryTime.put(entryValue, valueToEntryTime.get(entryValue) + 1);
            violations++;
        } else{
            valueToEntryTime.put(entryValue, 1);
        }
    }

    private void decreaseEntry(int entryValue){
        if(valueToEntryTime.containsKey(entryValue)){
            if(valueToEntryTime.get(entryValue) == 1){
                valueToEntryTime.remove(entryValue);
            } else {
                valueToEntryTime.put(entryValue, valueToEntryTime.get(entryValue) - 1);
                violations--;
            }
        }
    }

//    public static void main(String[] args) {
//        LocalSearchManager localSearchManager = new LocalSearchManager();
//        ConstraintSystem constraintSystem = new ConstraintSystem(localSearchManager);
//        IdpcnduConstraint constraint;
//        Random random = new Random(0);
//
//        VarIntLS[] varIntLS = new VarIntLS[10];
//        for(int i= 0; i< 10 ; i ++){
//            VarIntLS var = new VarIntLS(localSearchManager,0,10);
//            var.setValue(random.nextInt(8));
//            varIntLS[i] = var;
//        }
//        constraint = new IdpcnduConstraint(varIntLS);
//        constraintSystem.post(constraint);
//        localSearchManager.close();
//
//        for(int i =0 ;i < 10 ;i++){
//            System.out.println(varIntLS[i].getValue());
//        }
//        System.out.println();
//        System.out.println(constraintSystem.verify());
//        System.out.println();
//
//        for(int i =0 ;i < 10 ;i++){
//            varIntLS[i].setValuePropagate(random.nextInt(8));
//            System.out.println(varIntLS[i].getValue());
//        }
//        System.out.println();
//        System.out.println(constraintSystem.verify());
//        System.out.println();
//
//        for(int i =0 ;i < 10 ;i++){
//            System.out.println(constraint.valueCauseViolation(varIntLS[i].getValue()));
//        }
//    }
}
