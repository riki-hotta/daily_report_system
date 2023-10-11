package actions.views;

import java.util.ArrayList;
import java.util.List;

import models.Good;

/**
 * いいねした従業員データのDTOモデル⇔Viewモデルの変換を行うクラス
 *
 */
public class GoodConverter {
    /**
     * ViewモデルのインスタンスからDTOモデルのインスタンスを作成する
     * @param gv GoodViewのインスタンス
     * @return Goodのインスタンス
     */
    public static Good toModel(GoodView gv) {
        return new Good(
                gv.getId(),
                EmployeeConverter.toModel(gv.getEmployee()),
                ReportConverter.toModel(gv.getReport()),
                gv.getCreatedAt(),
                gv.getUpdatedAt());
    }

    /**
     * DTOモデルのインスタンスからViewモデルのインスタンスを作成する
     * @param g Goodのインスタンス
     * @return GoodViewのインスタンス
     */
    public static GoodView toView(Good g) {
        if(g == null) {
            return null;
        }

        return new GoodView(
                g.getId(),
                EmployeeConverter.toView(g.getEmployee()),
                ReportConverter.toView(g.getReport()),
                g.getCreatedAt(),
                g.getUpdatedAt());
    }

    /**
     * DTOモデルのリストからViewモデルのリストを作成する
     * @param list DTOモデルのリスト
     * @return Viewモデルのリスト
     */
    public static List<GoodView> toViewList(List<Good> list) {
        List<GoodView> gvs = new ArrayList<>();

        for (Good g : list) {
            gvs.add(toView(g));
        }

        return gvs;
    }

    /**
     * Viewモデルの全フィールドの内容をDTOモデルのフィールドにコピーする
     * @param g DTOモデル(コピー先)
     * @param gv Viewモデル(コピー元)
     */
    public static void copyViewToModel(Good g, GoodView gv) {
        g.setId(gv.getId());
        g.setEmployee(EmployeeConverter.toModel(gv.getEmployee()));
        g.setReport(ReportConverter.toModel(gv.getReport()));
        g.setCreatedAt(gv.getCreatedAt());
        g.setUpdatedAt(gv.getUpdatedAt());
    }
}
