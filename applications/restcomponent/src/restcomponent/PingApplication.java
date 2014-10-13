package restcomponent;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import restcomponent.MemberStatement;
import restcomponent.PingResource;

public class PingApplication extends Application {
  @Override
  public Set<Class<?>> getClasses() {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      classes.add(PingResource.class);
      classes.add(MemberStatement.class);
      return classes;
  }
}