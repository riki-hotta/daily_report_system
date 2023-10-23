package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.FollowView;
import actions.views.GoodView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import services.FollowService;
import services.GoodService;
import services.ReportService;

/**
 * 日報に関する処理を行うActionクラス
 *
 */
public class ReportAction extends ActionBase {

    private ReportService service;
    private GoodService goodservice;
    private FollowService followservice;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        service = new ReportService();
        goodservice = new GoodService();
        followservice = new FollowService();

        //メソッドを実行
        invoke();
        followservice.close();
        goodservice.close();
        service.close();
    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        //指定されたページ数の一覧画面に表示する日報データを取得
        int page = getPage();
        List<ReportView> reports = service.getAllPerPage(page);

        //全日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, reportsCount); //全ての日報データの件数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //一覧画面を表示
        forward(ForwardConst.FW_REP_INDEX);
    }

    /**
     * 新規登録画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException, IOException {

        putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン

        //日報情報の空インスタンスに、日報の日付＝今日の日付を設定する
        ReportView rv = new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT, rv); //日付のみ設定済みの日報インスタンス

        //新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);

    }

    /**
     * 新規登録を行う
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //日報の日付が入力されていなければ、今日の日付を設定
            LocalDate day = null;
            if (getRequestParam(AttributeConst.REP_DATE) == null
                    || getRequestParam(AttributeConst.REP_DATE).equals("")) {
                day = LocalDate.now();
            } else {
                day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            //パラメータの値をもとに日報情報のインスタンスを作成する
            ReportView rv = new ReportView(
                    null,
                    ev, //ログインしている従業員を、日報作成者として登録する
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null,
                    null,
                    0);

            //日報情報登録
            List<String> errors = service.create(rv);

            if (errors.size() > 0) {
                //登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv);//入力された日報情報
                putRequestScope(AttributeConst.ERR, errors);//エラーのリスト

                //新規登録画面を再表示
                forward(ForwardConst.FW_REP_NEW);

            } else {
                //登録中にエラーがなかった場合

                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
    }

    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void show() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //指定した日報のidとログインしている従業員のidの両方に一致するいいねした従業員の件数を取得する
        long countrepandemp = goodservice.countRepAndEmp(rv, ev);

        if (rv == null) {
            //該当の日報データが存在しない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        } else {

            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ
            putRequestScope(AttributeConst.GOOD_REP_EMP_COUNT, countrepandemp); //取得した日報にいいねした従業員データ

            //詳細画面を表示
            forward(ForwardConst.FW_REP_SHOW);
        }
    }

    /**
     * 編集画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void edit() throws ServletException, IOException {

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        if (rv == null || ev.getId() != rv.getEmployee().getId()) {
            //該当の日報データが存在しない、または
            //ログインしている従業員が日報の作成者でない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        } else {

            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ

            //編集画面を表示
            forward(ForwardConst.FW_REP_EDIT);
        }

    }

    /**
     * 更新を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //idを条件に日報データを取得する
            ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

            //入力された日報内容を設定する
            rv.setReportDate(toLocalDate(getRequestParam(AttributeConst.REP_DATE)));
            rv.setTitle(getRequestParam(AttributeConst.REP_TITLE));
            rv.setContent(getRequestParam(AttributeConst.REP_CONTENT));

            //日報データを更新する
            List<String> errors = service.update(rv);

            if (errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv); //入力された日報情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                //編集画面を再表示
                forward(ForwardConst.FW_REP_EDIT);
            } else {
                //更新中にエラーがなかった場合

                //セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);

            }
        }
    }

    /**
     * いいねを行う
     * @throws ServletException
     * @throws IOException
     */
    public void good() throws ServletException, IOException{
        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        // いいね数を1加算
        rv.setReportGood(rv.getReportGood() + 1);

        //日報データを更新する
        List<String> errors = service.update(rv);

        // いいねした従業員テーブルに登録する
        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //パラメータの値をもとにいいねした従業員情報のインスタンスを作成する
        GoodView gv = new GoodView(
                null,
                ev, //ログインしている従業員を、いいねした従業員として登録する
                rv, // 表示している日報を登録
                null,
                null);

        //いいねした従業員情報登録
        goodservice.create(gv);

        if (errors.size() > 0) {
            //更新中にエラーが発生した場合

            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.REPORT, rv); //入力された日報情報
            putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

            //詳細画面を再表示
            forward(ForwardConst.FW_REP_SHOW);
        } else {
            //更新中にエラーがなかった場合

            //セッションに更新完了のフラッシュメッセージを設定
            putSessionScope(AttributeConst.FLUSH, MessageConst.I_GOOD.getMessage());

            //一覧画面にリダイレクト
            redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
        }
    }

    /**
     * いいねした人一覧ページを表示する
     * @throws ServletException
     * @throws IOException
     */
    public void goodindex() throws ServletException, IOException {
        //idを条件に日報データを取得する
        ReportView idRv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //指定した日報にいいねした従業員を、指定されたページ数の一覧画面に表示する分取得
        int page = getPage();
        List<GoodView> goods = goodservice.getMinePerPage(idRv, page);

        //指定した日報にいいねした従業員の件数を取得
        long idRepCount = goodservice.countAllMine(idRv);

        putRequestScope(AttributeConst.REPORT, idRv); //取得した日報データ
        putRequestScope(AttributeConst.GOODS, goods); //取得した、日報にいいねした従業員データ
        putRequestScope(AttributeConst.GOOD_COUNT, idRepCount); //全ての、日報にいいねした従業員データの件数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //いいねした人一覧ページを表示
        forward(ForwardConst.FW_REP_GOODS);
    }

    /**
     * 日報詳細ページの「この日報の作成者をフォローする」リンクを押下時に、フォロー情報をデータベースに登録する
     * @throws ServletException
     * @throws IOException
     */
    public void follow() throws ServletException, IOException{
        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        // パラメータの値をもとにフォローした従業員情報のインスタンスを作成する
        FollowView fv = new FollowView(
                null,
                ev, //ログインしている従業員を、フォローした従業員として登録する
                rv.getEmployee(), // 日報を作成した従業員を、フォローされた従業員として登録する
                null,
                null);

        //フォローした従業員情報登録
        followservice.create(fv);

        //セッションにフォローしました、のフラッシュメッセージを設定
        putSessionScope(AttributeConst.FLUSH, MessageConst.I_FOLLOW.getMessage());

        //タイムラインページにリダイレクト
        redirect(ForwardConst.ACT_REP, ForwardConst.CMD_TIMELINE);
    }

    /**
     * タイムラインページを表示する
     * @throws ServletException
     * @throws IOException
     */
    public void timeline() throws ServletException, IOException {
        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        // ログイン中の従業員がフォローした従業員リストのデータを得る
        List<FollowView> follows = followservice.getFollowed(ev);

        // フォローした従業員のリストから、フォローされた従業員情報をそれぞれ抜き出す
        for (FollowView flw : follows) {
            // フォローされた従業員が作成した日報を、指定されたページ数の一覧画面に表示する分取得しReportViewのリストで返却する
            int page = getPage();
            List<ReportView> reports = followservice.getMinePerPage(flw.getFlwedemp(), page);

            //フォローされた従業員が作成した日報の件数を取得し、返却する
            long count = followservice.countAllMine(flw.getFlwedemp());

            putRequestScope(AttributeConst.FLWEMP, ev); //フォローした従業員情報
            putRequestScope(AttributeConst.FOLLOWS, follows); //フォローした従業員リスト
            putRequestScope(AttributeConst.FLWEDEMP, flw.getFlwedemp()); //フォローされた従業員情報
            putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
            putRequestScope(AttributeConst.REP_COUNT, count); //フォローされた従業員が作成した日報の件数
            putRequestScope(AttributeConst.PAGE, page); //ページ数
            putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

            //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
            String flush = getSessionScope(AttributeConst.FLUSH);
            if (flush != null) {
                putRequestScope(AttributeConst.FLUSH, flush);
                removeSessionScope(AttributeConst.FLUSH);
            }

            //タイムラインページを表示
            forward(ForwardConst.FW_REP_TIMELINE);
        }

    }

}