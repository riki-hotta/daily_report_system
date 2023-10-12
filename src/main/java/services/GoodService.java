package services;

import java.time.LocalDateTime;
import java.util.List;

import actions.views.GoodConverter;
import actions.views.GoodView;
import actions.views.ReportConverter;
import actions.views.ReportView;
import constants.JpaConst;
import models.Good;

/**
 * いいねした従業員テーブルの操作に関わる処理を行うクラス
 */
public class GoodService extends ServiceBase {
    /**
     * 指定した日報にいいねした従業員を、指定されたページ数の一覧画面に表示する分取得しGoodViewのリストで返却する
     * @param report 日報
     * @param page ページ数
     * @return 一覧画面に表示するデータのリスト
     */
    public List<GoodView> getMinePerPage(ReportView report, int page){
        List<Good> goods = em.createNamedQuery(JpaConst.Q_GOOD_GET_ALL_MINE, Good.class)
                .setParameter(JpaConst.JPQL_PARM_REPORT, ReportConverter.toModel(report))
                .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                .setMaxResults(JpaConst.ROW_PER_PAGE)
                .getResultList();
        return GoodConverter.toViewList(goods);
    }

    /**
     * 指定した日報にいいねした従業員の件数を取得し、返却する
     * @param report
     * @return 従業員の件数
     */
    public long countAllMine(ReportView report) {
        long count = (long) em.createNamedQuery(JpaConst.Q_GOOD_COUNT_ALL_MINE, Long.class)
                .setParameter(JpaConst.JPQL_PARM_REPORT, ReportConverter.toModel(report))
                .getSingleResult();
        return count;
    }

    /**
     * いいねするリンクが押下された際に、いいねした従業員テーブルに登録する
     * @param gv いいねした従業員の登録内容
     * @return 空のエラーのリスト
     */
    public List<String> create(GoodView gv) {
        List<String> gooderrors = null;
        LocalDateTime ldt = LocalDateTime.now();
        gv.setCreatedAt(ldt);
        gv.setUpdatedAt(ldt);
        createInternal(gv);

        return gooderrors;
    }

    /**
     * 日報データを1件登録する
     * @param rv 日報データ
     */
    private void createInternal(GoodView gv) {
        em.getTransaction().begin();
        em.persist(GoodConverter.toModel(gv));
        em.getTransaction().commit();
    }
}
