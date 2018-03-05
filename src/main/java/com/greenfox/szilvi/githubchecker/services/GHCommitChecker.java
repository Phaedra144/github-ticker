package com.greenfox.szilvi.githubchecker.services;

import com.greenfox.szilvi.githubchecker.entities.ClassGithub;
import com.greenfox.szilvi.githubchecker.httpconnection.GitHubRetrofit;
import com.greenfox.szilvi.githubchecker.models.Comment;
import com.greenfox.szilvi.githubchecker.models.GfCommits;
import com.greenfox.szilvi.githubchecker.repositories.GithubHandleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import static com.greenfox.szilvi.githubchecker.services.Settings.*;


@Service
public class GHCommitChecker {

    @Autowired
    GithubHandleRepo classGithubRepo;

    GitHubRetrofit gitHubRetrofit = new GitHubRetrofit();
    CheckDates checkDates = new CheckDates();

    public List<String> checkRepos(List<String> ghHandles) throws IOException {
        if(ghHandles.get(0).substring(0, 2).equals(",,")){
            String firstGhHandle = cutFirstChar(ghHandles.get(0));
            ghHandles.remove(0);
            ghHandles.add(0, firstGhHandle);
        }
        return ghHandles;
    }

    private String cutFirstChar(String firstGhHandle) {
        return firstGhHandle.substring(2);
    }

    public HashMap<String, List<Integer>> fillMapWithRepoRelevantStats(List<String> classRepos, String startDate, String endDate) throws IOException {
        List<GfCommits> gfCommits;
        List<Comment> gfComments;
        HashMap<String, List<Integer>> githubThingsHashMap = new HashMap<>();
        for (int i = 0; i < classRepos.size(); i++) {
            List<Integer> counts = new ArrayList<>();
            gfCommits = getPreviousWeekCommits(classRepos.get(i), startDate, endDate);
            gfComments = getComments(classRepos.get(i));
            int nrOfComments = checkAndSaveIfNewComment(gfComments.size(), classRepos.get(i));
            int noCommitDays = checkDates.checkHowManyDaysNotCommitted(gfCommits, startDate, endDate);
            counts.add(noCommitDays);
            counts.add(gfCommits.size());
            counts.add(nrOfComments);
            githubThingsHashMap.put(classRepos.get(i), counts);
        }
        return githubThingsHashMap;
    }

    public List<GfCommits> getPreviousWeekCommits(String repoName, String startDate, String endDate) throws IOException {
        startDate = getStringStartDate(startDate);
        endDate = getStringEndDate(endDate);
        Call<List<GfCommits>> gfCommitsCall = gitHubRetrofit.getService().getClassCommits(GITHUB_ORG, repoName, startDate, endDate);
        return gfCommitsCall.execute().body();
    }

    public List<Comment> getComments(String repoName) throws IOException {
        Call<List<Comment>> gfComments = gitHubRetrofit.getService().getCommentsOnRepos(GITHUB_ORG, repoName);
        return gfComments.execute().body();
    }

    public ArrayList<Integer> getTotalStats(HashMap<String, List<Integer>> repoHashMap) {
        ArrayList<Integer> totals = new ArrayList<>();
        int noCommits = 0;
        int commits = 0;
        int comments = 0;
        for (Map.Entry entry : repoHashMap.entrySet()) {
            List<Integer> counts = (List<Integer>) entry.getValue();
            noCommits = noCommits + counts.get(0);
            commits = commits + counts.get(1);
            comments = comments + counts.get(2);
        }
        totals.add(noCommits);
        totals.add(commits);
        totals.add(comments);
        return totals;
    }

    private int checkAndSaveIfNewComment(int numberOfComments, String repo) {
        ClassGithub classGithub = classGithubRepo.findByGithubHandle(repo);
        int difference = 0;
        if (classGithub.getNumberOfComments() != numberOfComments) {
            difference = numberOfComments - classGithub.getNumberOfComments();
            classGithub.setNumberOfComments(numberOfComments);
            classGithubRepo.save(classGithub);
        }
        return difference;
    }

    public List<String> ghHandlesToString(List<ClassGithub> ghHandlesByClass) {
        List<String> ghHandles = new ArrayList<>();
        for (ClassGithub ch:ghHandlesByClass) {
            ghHandles.add(ch.getGithubHandle());
        }
        return ghHandles;
    }

    private String getStringEndDate(String endDate) {
        LocalDate endD = checkDates.convertToLocalDate(endDate).plusDays(1);
        endDate = endD.toString();
        return endDate;
    }

    private String getStringStartDate(String startDate) {
        LocalDate startD = checkDates.convertToLocalDate(startDate).minusDays(1);
        startDate = startD.toString();
        return startDate;
    }
}
