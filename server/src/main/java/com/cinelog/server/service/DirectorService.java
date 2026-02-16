package com.cinelog.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Director;
import com.cinelog.server.dto.director.DirectorSearchResult;
import com.cinelog.server.exception.director.DirectorNotFoundException;
import com.cinelog.server.repository.DirectorRepository;
import com.cinelog.server.repository.MovieRepository;

@Service
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;
    public DirectorService(DirectorRepository directorRepository,MovieRepository movieRepository){
        this.directorRepository=directorRepository;
        this.movieRepository = movieRepository;
    }
    
    @Transactional
    public void createDirector(String directorName){
        Director director = new Director(directorName);
        directorRepository.save(director); 
    }
    
    public List<DirectorSearchResult> findAllDirectorsByKeyword(String keyword){
        List<Director> directors= directorRepository.findAllByNameContaining(keyword);
        List<DirectorSearchResult> results = new ArrayList<>();
        for(Director director:directors){
            results.add(new DirectorSearchResult(director.getName(), director.getId()));
        }
        return results;
    }
    public List<Director> findAllDirectors(){
        return directorRepository.findAll();
    }
    public Director getDirectorById(Long id){//get은 반드시 가져오는것
        return directorRepository.findById(id).orElseThrow(()->new DirectorNotFoundException(id));
    }

    @Transactional
    public void updateDirector(Long id,String name){
        Director director = getDirectorById(id);
        director.changeName(name);
        directorRepository.save(director);
    }

    @Transactional
    public void deleteDirector(Long id){//영화와의 관계가 있기때문에 삭제시 주의 해야함
        long movieCount = movieRepository.countByDirectorId(id);
        if (movieCount > 0) {
            throw new IllegalStateException("해당 감독의 영화가 " + movieCount + "편 존재하여 삭제할 수 없습니다.");
        }
        if(!directorRepository.delete(id))throw new DirectorNotFoundException(id);
    }
}