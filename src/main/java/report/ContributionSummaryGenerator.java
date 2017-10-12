package report;

import dataObject.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by matanghao1 on 4/10/17.
 */
public class ContributionSummaryGenerator {

    public static List<RepoContributionSummary> analyzeContribution(List<RepoInfo> repos){
        System.out.println("Generating summary report...");
        List<RepoContributionSummary> result = new ArrayList<>();
        for (RepoInfo repo:repos){
            RepoContributionSummary summary = new RepoContributionSummary(repo);
            summary.setAuthorIntervalContributions(getAuthorIntervalContributions(repo.getCommits()));
            summary.setAuthorFinalContributionMap(repo.getCommits().get(repo.getCommits().size()-1).getAuthorContributionMap());
            result.add(summary);
        }
        System.out.println("done");
        return result;
    }

    private static Map<Author, List<AuthorIntervalContribution>> getAuthorIntervalContributions(List<CommitInfo> commits){
        //init
        long durationDays = getDurationInDays(commits);
        Map<Author, List<AuthorIntervalContribution>> result = new HashMap<>();
        for (Author author: commits.get(commits.size()-1).getAuthorContributionMap().keySet()){
            result.put(author,new ArrayList<>());
        }
        Date currentDate = commits.get(0).getTime();
        Date nextDate = getNextCutoffDate(currentDate, durationDays);

        initIntervalContributionForNewDate(result, currentDate);
        for (CommitInfo commit: commits){
            if (nextDate.before(commit.getTime())){
                currentDate = new Date(nextDate.getTime());
                nextDate = getNextCutoffDate(nextDate, durationDays);
                initIntervalContributionForNewDate(result,currentDate);
            }
            List<AuthorIntervalContribution> tempList = result.get(commit.getAuthor());
            tempList.get(tempList.size()-1).updateForCommit(commit);
        }
        return result;
    }

    private static void initIntervalContributionForNewDate(Map<Author, List<AuthorIntervalContribution>> map, Date date){
        for (List<AuthorIntervalContribution> dateToInvertal : map.values()){
            //dials back one minute so that github api can include the commit on the time itself
            dateToInvertal.add(new AuthorIntervalContribution(getOneMinuteBefore(date),0,0));
        }
    }

    private static long getDurationInDays(List<CommitInfo> commits){
        long duration = commits.get(commits.size()-1).getTime().getTime() - commits.get(0).getTime().getTime();
        return TimeUnit.DAYS.convert(duration, TimeUnit.MILLISECONDS);

    }

    private static Date getOneMinuteBefore(Date current){
        Calendar c = Calendar.getInstance();
        c.setTime(current);
        c.add(Calendar.MINUTE,-1);
        return c.getTime();
    }
    private static Date getNextCutoffDate(Date current, long totalDuration){
        Calendar c = Calendar.getInstance();
        c.setTime(current);
        if (totalDuration < 30){
            c.add(Calendar.DATE,1);
        } else if (totalDuration < 180) {
            c.add(Calendar.DATE,7);
        } else{
            c.add(Calendar.MONTH,1);
        }
        return c.getTime();
    }
}