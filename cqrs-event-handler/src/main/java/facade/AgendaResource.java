package facade;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.InsertOneResult;
import models.Customer;
import models.Resource;
import models.in.CustomerAddress;
import org.bson.BsonString;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

@ApplicationScoped
public class AgendaResource
{
    @Inject MongoClient mongoClient;

    //Collection getters
    public MongoCollection<Document> getCoursesCollection(){return mongoClient.getDatabase("courses").getCollection("courses");}
    public MongoCollection<Document> getCustomerCollection(){return mongoClient.getDatabase("references").getCollection("customers");}
    public MongoCollection<Document> getResourcesCollection(){return mongoClient.getDatabase("references").getCollection("resources");}
    public MongoCollection<Document> getTeacherCollection(){return mongoClient.getDatabase("references").getCollection("teachers");}
    public MongoCollection<Document> getTeacherCollectionById(String teacherId){return mongoClient.getDatabase("teachers").getCollection(teacherId);}
    public MongoCollection<Document> getCoursesCollectionByCustomerId(String customerId){return mongoClient.getDatabase("courses").getCollection(customerId);}
    public MongoCollection<Document> getResourceCollectionById(String resourceId){return mongoClient.getDatabase("resources").getCollection(resourceId);}

    public void createCustomerCollection(String customerId)
    {
        mongoClient.getDatabase("courses").createCollection(customerId);
    }

    public void createResourceCollection(String resourceId)
    {
        mongoClient.getDatabase("resources").createCollection(resourceId);
    }

    public void createTeacherCollection(String teacherId)
    {
        mongoClient.getDatabase("teachers").createCollection(teacherId);
    }

    //Creation d'un cours
    public void createEvent(UUID id, String type, LocalDateTime startDateTime, LocalDateTime endDateTime, int nbMaxParticipant)
    {
        InsertOneResult res = getCoursesCollection().insertOne(new Document()
                .append("_id",id.toString())
                .append("type",type)
                .append("startDateTime",startDateTime)
                .append("endDateTime",endDateTime)
                .append("nbMaxParticipant",nbMaxParticipant)
                .append("customers",new ArrayList<String>())
                .append("resources",new ArrayList<String>()));

        System.out.println("Acknowledged : " + res.wasAcknowledged());
    }

    //Creation d'un client
    public void createCustomer(UUID customerId, String firstname, String lastname, Date birthdate, CustomerAddress address)
    {
        //Creation de la collection d??di??e
        createCustomerCollection(customerId.toString());

        //Creation du client dans la collection clients
        getCustomerCollection().insertOne(new Document()
                .append("_id",customerId.toString())
                .append("firstname",firstname)
                .append("lastname",lastname)
                .append("birthdate",birthdate)
                .append("address",address.toString())
        );
    }

    public void updateCustomer(UUID customerId, String firstname, String lastname, Date birthdate, CustomerAddress address)
    {
        getCustomerCollection().findOneAndUpdate(
                new Document().append("_id", customerId.toString()),
                new Document().append("$set", new Document().append("firstname",firstname)
                        .append("lastname",lastname)
                        .append("birthdate",birthdate)
                        .append("address",address))
        );
    }

    //Inscription d'un membre ?? un cours
    public void enrollCustomer(UUID eventId, UUID customerId)
    {
        //Ajout du client sur le cours
        Document d = getCoursesCollection().findOneAndUpdate(new Document().append("_id",eventId.toString())
                ,new Document().append("$push", new Document().append("customers",customerId.toString()))
                ,new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));

        //Ajout de l'??l??ment d'agenda dans le calendrier du client
        getCoursesCollectionByCustomerId(customerId.toString()).insertOne(new Document()
                .append("_id",d.getString("_id"))
                .append("type",d.getString("type"))
                .append("startDateTime",d.getDate("startDateTime"))
                .append("endDateTime",d.getDate("endDateTime"))
                .append("teacherFirstname",d.getString("teacherFirstname"))
                .append("teacherLastname",d.getString("teacherLastname"))
        );

        //Ajouts du nom pr??nom du client dans l'??l??ment de calendrier du prof
        if(!"".equals(d.getString("teacherId")) || !(d.getString("teacherId") == null)) //il y a un prof sur le cours
        {
            Document d_customer = getCustomerCollection().find(new Document().append("_id",customerId.toString())).cursor().next();
            getTeacherCollectionById(d.getString("teacherId")).findOneAndUpdate(
                    new Document().append("_id",eventId.toString())
                    ,new Document().append("$push", new Document().append("customers",d_customer.getString("firstname") + " " + d_customer.getString("lastname")))
            );
        }
    }

    //D??sinscription d'un membre ?? un cours
    public void unrollCustomer(UUID eventId, UUID customerId)
    {
        //Suppression dans la liste des participants du cours
        Document d = getCoursesCollection().findOneAndUpdate(new Document().append("_id",eventId.toString())
                ,new Document().append("$pull", new Document().append("customers",customerId.toString()))
                ,new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));

        //Suppression de l'??l??ment dans le calendrier du client
        getCoursesCollectionByCustomerId(customerId.toString()).deleteOne(new Document().append("_id",eventId.toString()));
    }

    public void deleteEvent(UUID courseId)
    {
        try
        {
            Document courseDoc = getCoursesCollection().find(new Document().append("_id",courseId.toString())).cursor().next();
            for (Customer c : courseDoc.getList("customers",Customer.class))
            {
                //GetCollection of the actual customer and delete course in his view
                getCoursesCollectionByCustomerId(c.getId().toString()).deleteOne(new Document().append("_id",courseId.toString()));
            }
            //Course deletion
            getCoursesCollection().deleteOne(new Document().append("_id", courseId.toString()));
        }
        catch(NoSuchElementException e)
        {
            System.out.println("L'??l??ment ?? supprimer n'a pas ??t?? trouv??");
        }
    }

    public void updateEvent(UUID eventId)
    {
        getCoursesCollection().updateOne(new Document("_id",eventId.toString()),new Document());
    }

    public void createTeacher(UUID id, String lastname, String firstname)
    {
        getTeacherCollection().insertOne(new Document()
                .append("_id", id.toString())
                .append("lastname",lastname)
                .append("firstname",firstname)
        );
        createTeacherCollection(id.toString());
    }

    public void assignTeacher(UUID coursesId, UUID teacherId)
    {

        Document teachDoc = getTeacherCollection().find(new Document().append("_id",teacherId.toString())).cursor().next();
        Document courseDoc = getCoursesCollection().findOneAndUpdate(new Document().append("_id",coursesId.toString()),
                new Document("$set", new Document().append("teacherId",teacherId.toString())
                        .append("teacherFirstname",teachDoc.getString("firstname"))
                        .append("teacherLastname",teachDoc.getString("lastname"))));


        getTeacherCollectionById(teacherId.toString()).insertOne(
                new Document()
                        .append("_id",courseDoc.getString("_id"))
                        .append("type",courseDoc.getString("type"))
                        .append("startDateTime",courseDoc.getDate("startDateTime"))
                        .append("endDateTime",courseDoc.getDate("endDateTime"))
                        .append("resources",courseDoc.getList("resources", String.class))
                        .append("customers",new ArrayList<String>())
        );

        for (Customer customer : courseDoc.getList("customers", Customer.class))
        {
            getCoursesCollectionByCustomerId(customer.getId().toString()).findOneAndUpdate(new Document().append("_id",coursesId.toString())
                    ,new Document()
                    .append("teacherFirstname",teachDoc.getString("firstname"))
                    .append("teacherLastname",teachDoc.getString("lastname")));

            Document d_customer = getCustomerCollection().find(new Document().append("_id",customer.getId().toString())).cursor().next();

            getTeacherCollectionById(teacherId.toString()).findOneAndUpdate(
                    new Document().append("_id", coursesId.toString()),
                    new Document().append("$push",
                            new Document()
                                    .append("customers",d_customer.getString("firstname") +" "+ d_customer.getString("lastname")))

            );
        }
    }

    public void createResource(UUID resourceId, String name)
    {
        //Creation dans le r??f??rentiel
        getResourcesCollection().insertOne(new Document()
                .append("_id",resourceId.toString())
                .append("name", name));

        //Creation du calendrier de la resource
        createResourceCollection(resourceId.toString());
    }

    public void addResourceToEvent(UUID eventId, UUID resourceId)
    {
        //TODO Rechercher info resources dans refResources puis ajouter les info dans l'event
        MongoCursor<Document> cursor = getResourcesCollection().find(new Document().append("_id", new BsonString(resourceId.toString()))).cursor();
        Document doc;

        try{
            doc = cursor.next();
        }catch(NoSuchElementException e)
        {
            System.out.println("La ressource n'existe pas dans le r??f??rentiel");
            return;
        }

        Resource resource = new Resource(UUID.fromString(doc.getString("_id")), doc.getString("name"));

        //On ajoute le nom de la resource dans le cours
        Document updatedCourse = getCoursesCollection().findOneAndUpdate(new Document().append("_id",eventId.toString()),
                new Document().append("$push", new Document().append("resources",resource.getName())),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                );

        //On ajoute un ??l??ment dans le calendrier de la resource
        getResourceCollectionById(resourceId.toString()).insertOne(
                new Document().append("courseId", updatedCourse.getString("_id"))
                        .append("courseType",updatedCourse.getString("type"))
                        .append("startDateTime", updatedCourse.getDate("startDateTime"))
                        .append("endDateTime",updatedCourse.getDate("endDateTime"))
        );

    }
}