package org.example.simple.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Document("products")
public class Products {

   @Id
   @Indexed
   private String id;
   @Indexed
   private String groupId;
   private String name;
   private Date createdDate;


   public String getGroupId() {
      return groupId;
   }

   public void setGroupId(String groupId) {
      this.groupId = groupId;
   }

   public static Products[] RandomProducts(int count, String groupId) {
      Products[] l = new Products[count];
      int i = 0;
      while (i < count) {
         Products p = new Products();
         UUID uuid = UUID.randomUUID();
         p.setId(uuid.toString());
         p.setGroupId(groupId);

         p.setName(i + "-Name");
         p.setCreatedDate(new Date());
         l[i] = p;
         i++;
      }
      return l;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Date getCreatedDate() {
      return createdDate;
   }

   public void setCreatedDate(Date createdDate) {
      this.createdDate = createdDate;
   }


   @Override
   public String toString() {
      return "Products{" +
              "id='" + id + '\'' +
              ", groupId='" + groupId + '\'' +
              ", name='" + name + '\'' +
              ", createdDate=" + createdDate +
              '}';
   }
}

