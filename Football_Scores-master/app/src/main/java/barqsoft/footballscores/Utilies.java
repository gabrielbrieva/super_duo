package barqsoft.footballscores;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{
    public static final String URL_END_POINT = "http://api.football-data.org/alpha/";

    public static final int PREMIER_LEGAUE = 398;
    public static final int CHAMPIONS_LEAGUE = 405;

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";

    /**
     * Method to check if the device have access to internet
     * @param context
     * @return true if internet connection is available
     */
    public static boolean isNetworkConnected(Context context) {

        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        }

        return false;
    }

    public static int getLeague(int league_num)
    {
        switch (league_num)
        {
            case PREMIER_LEGAUE : return  R.string.league_premier;
            case CHAMPIONS_LEAGUE : return R.string.league_champions;
            default: return -1;
        }
    }

    public static int getMatchDay(int match_day)
    {
        if (match_day <= 6)
        {
            return R.string.pleague_stage_group_stage;
        }
        else if(match_day == 7 || match_day == 8)
        {
            return R.string.pleague_stage_knockout;
        }
        else if(match_day == 9 || match_day == 10)
        {
            return R.string.pleague_stage_quarterfinal;
        }
        else if(match_day == 11 || match_day == 12)
        {
            return R.string.pleague_stage_semifinal;
        }
        else
        {
            return R.string.pleague_stage_final;
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
            return " - ";
        else
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        if (teamname==null)
            return R.drawable.no_icon;

        switch (teamname)
        {
            case "Arsenal FC" : return R.drawable.arsenal_fc;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City FC" : return R.drawable.swansea_city_afc;
            case "Leicester City FC" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion FC" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            case "Newcastle United FC" : return R.drawable.newcastle_united;
            case "Aston Villa FC" : return R.drawable.aston_villa;
            case "Chelsea FC" : return R.drawable.chelsea;
            case "Crystal Palace FC" : return R.drawable.crystal_palace_fc;
            case "Liverpool FC": return R.drawable.liverpool;
            case "Manchester City FC": return R.drawable.manchester_city;
            case "Southampton FC": return R.drawable.southampton_fc;
            case "Real Madrid CF": return R.drawable.real_madrid_cf;
            case "Norwich City FC": return R.drawable.norwich_city_fc;
            case "Watford FC": return R.drawable.watford_fc;
            case "AFC Bournemouth": return R.drawable.afc_bournemouth;
            case "Paris Saint-Germain": return R.drawable.paris_saint_germain;
            case "Malmö FF": return R.drawable.malm_ff;
            case "Benfica Lissabon": return R.drawable.benfica_lissabon;
            case "FC Astana": return R.drawable.fc_astana;
            case "Sevilla FC": return R.drawable.sevilla_cf;
            case "Bor. Mönchengladbach": return R.drawable.borussia_mnchengladbach;
            case "Juventus Turin": return R.drawable.juventus_turin;
            case "Galatasaray SK": return R.drawable.galatasaray_sk;
            case "Club Atlético de Madrid": return R.drawable.atletico_madrid;
            case "VfL Wolfsburg": return R.drawable.vfl_wolfsburg;
            case "CSKA Moscow": return R.drawable.cska_moscow;
            case "PSV Eindhoven": return R.drawable.psv_eindhoven;
            case "Shakhtar Donetsk": return R.drawable.shakhtar_donetsk;
            case "Valencia CF": return R.drawable.valencia_fc;
            case "FC Zenit St. Petersburg": return R.drawable.zenit;
            case "Dynamo Kyiv": return R.drawable.dynamo_kyiv;
            case "FC Porto": return R.drawable.fc_porto;
            case "KAA Gent": return R.drawable.kaa_gent;
            case "GNK Dinamo Zagreb": return R.drawable.dinamo_zagreb;
            case "Olympique Lyonnais": return R.drawable.olympique_lyon;
            case "FC Bayern München": return R.drawable.fc_bayern_mnchen;
            case "AS Roma": return R.drawable.as_rom;
            case "FC Barcelona": return R.drawable.fc_barcelona;
            case "Maccabi Tel Aviv": return R.drawable.mtafc;
            case "FK BATE Baryssau": return R.drawable.bate_baryssau;
            case "Bayer Leverkusen": return R.drawable.bayer_leverkusen;
            case "Olympiacos F.C.": return R.drawable.olympiakos_pirus;
            default: return R.drawable.no_icon;
        }
    }
}
