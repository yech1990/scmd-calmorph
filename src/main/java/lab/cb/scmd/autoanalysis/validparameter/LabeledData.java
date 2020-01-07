package lab.cb.scmd.autoanalysis.validparameter;


class LabeledData {
    private double d_;
    private int label_;

    LabeledData(Double d, int label) {
        d_ = d;
        label_ = label;
    }

    double get_value() {
        return d_;
    }

    int get_label() {
        return label_;
    }

}
