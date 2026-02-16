package com.cinelog.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.dto.actor.ActorSearchResult;
import com.cinelog.server.exception.actor.ActorNotFoundException;
import com.cinelog.server.repository.ActorRepository;

@Service
@Transactional(readOnly = true)
public class ActorService {
    private final ActorRepository actorRepository;
    public ActorService(ActorRepository actorRepository){
        this.actorRepository = actorRepository;
    }

    @Transactional
    public Actor createActor(String actorName){
        Actor actor = new Actor(actorName);
        actorRepository.save(actor); 
        return actor;
    }

    public List<ActorSearchResult> findAllActorsByKeyword(String keyword){
        List<Actor> actors= actorRepository.findAllByNameContaining(keyword);
        List<ActorSearchResult> results = new ArrayList<>();
        for(Actor actor:actors){
            results.add(new ActorSearchResult(actor.getName(), actor.getId()));
        }
        return results;
    }
    public List<Actor> findAllActors(){
        return actorRepository.findAll();
    }
    public Actor getActorById(Long id){
        return actorRepository.findById(id).orElseThrow(()->new ActorNotFoundException(id));
    }

    @Transactional
    public void updateActor(Long id,String newName){
        Actor actor = getActorById(id);
        actor.changeName(newName);
        actorRepository.save(actor);
    }

    @Transactional
    public void deleteActor(Long id){
        if(!actorRepository.delete(id)) throw new ActorNotFoundException(id);
    }
}