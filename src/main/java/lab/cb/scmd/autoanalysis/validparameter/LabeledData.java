package lab.cb.scmd.autoanalysis.validparameter;


public class LabeledData {
    double d_;
    int label_;

    public LabeledData(Double d, int label) {
        d_ = d.doubleValue();
        label_ = label;
    }

    public double get_value() {
        return d_;
    }

    public int get_label() {
        return label_;
    }

}
