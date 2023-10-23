package services;

import java.time.LocalDateTime;
import java.util.List;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import actions.views.FollowConverter;
import actions.views.FollowView;
import actions.views.ReportConverter;
import actions.views.ReportView;
import constants.JpaConst;
import models.Report;

/**
 * フォローした従業員テーブルの操作に関わる処理を行うクラス
 */
public class FollowService extends ServiceBase {
    /**
     * ログイン中の従業員がフォローした従業員が作成した日報を、指定されたページ数の一覧画面に表示する分取得しReportViewのリストで返却する
     * @param ev フォローされた従業員
     * @param page ページ数
     * @return 一覧画面に表示するデータのリスト
     */
    public List<ReportView> getFollowAll(EmployeeView ev, int page){
        List<Report> reports = em.createNamedQuery(JpaConst.Q_FOLLOW_GET_ALL, Report.class)
                .setParameter(JpaConst.JPQL_PARM_EMPLOYEE, EmployeeConverter.toModel(ev))
                .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                .setMaxResults(JpaConst.ROW_PER_PAGE)
                .getResultList();
        return ReportConverter.toViewList(reports);
    }

    /**
     * ログイン中の従業員がフォローした従業員が作成した日報の件数を取得し、返却する
     * @param ev
     * @return 日報の件数
     */
    public long countFollowAll(EmployeeView ev) {
        long count = (long) em.createNamedQuery(JpaConst.Q_FOLLOW_COUNT_ALL, Long.class)
                .setParameter(JpaConst.JPQL_PARM_EMPLOYEE, EmployeeConverter.toModel(ev))
                .getSingleResult();
        return count;
    }

    /**
     * この日報の作成者をフォローする、リンクが押下された際に、フォローした従業員テーブルに登録する
     * @param fv フォローした従業員の登録内容
     */
    public void create(FollowView fv) {
        LocalDateTime ldt = LocalDateTime.now();
        fv.setCreatedAt(ldt);
        fv.setUpdatedAt(ldt);
        createInternal(fv);
    }

    /**
     * フォローした従業員データを1件登録する
     * @param fv フォローした従業員
     */
    private void createInternal(FollowView fv) {
        em.getTransaction().begin();
        em.persist(FollowConverter.toModel(fv));
        em.getTransaction().commit();
    }

}
