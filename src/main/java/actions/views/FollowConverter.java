package actions.views;

import java.util.ArrayList;
import java.util.List;

import models.Follow;

/**
 * フォローした従業員データのDTOモデル⇔Viewモデルの変換を行うクラス
 *
 */
public class FollowConverter {
    /**
     * ViewモデルのインスタンスからDTOモデルのインスタンスを作成する
     * @param fv FollowViewのインスタンス
     * @return Followのインスタンス
     */
    public static Follow toModel(FollowView fv) {
        return new Follow(
                fv.getId(),
                EmployeeConverter.toModel(fv.getFlwemp()),
                EmployeeConverter.toModel(fv.getFlwedemp()),
                fv.getCreatedAt(),
                fv.getUpdatedAt());
    }

    /**
     * DTOモデルのインスタンスからViewモデルのインスタンスを作成する
     * @param f Followのインスタンス
     * @return FollowViewのインスタンス
     */
    public static FollowView toView(Follow f) {
        if(f == null) {
            return null;
        }

        return new FollowView(
                f.getId(),
                EmployeeConverter.toView(f.getFlwemp()),
                EmployeeConverter.toView(f.getFlwedemp()),
                f.getCreatedAt(),
                f.getUpdatedAt());
    }

    /**
     * DTOモデルのリストからViewモデルのリストを作成する
     * @param list DTOモデルのリスト
     * @return Viewモデルのリスト
     */
    public static List<FollowView> toViewList(List<Follow> list){
        List<FollowView> fvs = new ArrayList<>();

        for(Follow f : list) {
            fvs.add(toView(f));
        }
        return fvs;
    }

    /**
     * Viewモデルの全フィールドの内容をDTOモデルのフィールドにコピーする
     * @param f DTOモデル(コピー先)
     * @param fv Viewモデル(コピー元)
     */
    public static void copyViewToModel(Follow f, FollowView fv) {
        f.setId(fv.getId());
        f.setFlwemp(EmployeeConverter.toModel(fv.getFlwemp()));
        f.setFlwedemp(EmployeeConverter.toModel(fv.getFlwedemp()));
        f.setCreatedAt(fv.getCreatedAt());
        f.setUpdatedAt(fv.getUpdatedAt());
    }
}
