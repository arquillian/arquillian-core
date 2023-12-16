/*
* JBoss, Home of Professional Open Source.
* Copyright 2012, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.arquillian.container.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class NetworkUtilsTestCase {

    @Test
    public void testFormatIPv6Test() {
        checkSameFormat("localhost");
        checkSameFormat("127.0.0.1");
        checkSameFormat("www.jboss.org");
        checkSameFormat("[::1]");
        checkSameFormat("[fe80::200:f8ff:fe21:67cf]");
        checkEqualFormat("[::1]", "::1");
        checkEqualFormat("[fe80::200:f8ff:fe21:67cf]", "fe80::200:f8ff:fe21:67cf");
    }

    @Test
    public void testFormatInetAddress() throws Exception{
        InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
        Assert.assertEquals("127.0.0.1", NetworkUtils.formatAddress(inetAddress));
        
        inetAddress = InetAddress.getByName("0:0:0:0:0:0:0:1");
        Assert.assertEquals("::1", NetworkUtils.formatAddress(inetAddress));
        
        inetAddress = InetAddress.getByName("fe80:0:0:0:f24d:a2ff:fe63:5766");
        Assert.assertEquals("fe80::f24d:a2ff:fe63:5766", NetworkUtils.formatAddress(inetAddress));
        
        inetAddress = InetAddress.getByName("1:0:0:1:0:0:0:1");
        Assert.assertEquals("1:0:0:1::1", NetworkUtils.formatAddress(inetAddress));
        
        inetAddress = InetAddress.getByName("1:0:0:1:1:0:0:1");
        Assert.assertEquals("1::1:1:0:0:1", NetworkUtils.formatAddress(inetAddress));
    }

    @Test
    public void testFormatSocketAddress() throws Exception {
        InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress,8000);
        Assert.assertEquals("127.0.0.1:8000", NetworkUtils.formatAddress(socketAddress));
        
        inetAddress = InetAddress.getByName("0:0:0:0:0:0:0:1");
        socketAddress = new InetSocketAddress(inetAddress,8000);
        Assert.assertEquals("[::1]:8000", NetworkUtils.formatAddress(socketAddress));
        
        inetAddress = InetAddress.getByName("fe80:0:0:0:f24d:a2ff:fe63:5766");
        socketAddress = new InetSocketAddress(inetAddress,8000);
        Assert.assertEquals("[fe80::f24d:a2ff:fe63:5766]:8000", NetworkUtils.formatAddress(socketAddress));
        
        inetAddress = InetAddress.getByName("1:0:0:1:0:0:0:1");
        socketAddress = new InetSocketAddress(inetAddress,8000);
        Assert.assertEquals("[1:0:0:1::1]:8000", NetworkUtils.formatAddress(socketAddress));
        
        inetAddress = InetAddress.getByName("1:0:0:1:1:0:0:1");
        socketAddress = new InetSocketAddress(inetAddress,8000);
        Assert.assertEquals("[1::1:1:0:0:1]:8000", NetworkUtils.formatAddress(socketAddress));
    }
    
    @Test
    public void testMayBeIPv6Address() {
        Assert.assertFalse(NetworkUtils.mayBeIPv6Address(null));

        Assert.assertTrue(NetworkUtils.mayBeIPv6Address("::1"));
        Assert.assertTrue(NetworkUtils.mayBeIPv6Address("::"));
        Assert.assertTrue(NetworkUtils.mayBeIPv6Address("2001:db8:0:0:1:0:0:1"));

        Assert.assertFalse(NetworkUtils.mayBeIPv6Address(""));
        Assert.assertFalse(NetworkUtils.mayBeIPv6Address(":1"));
        Assert.assertFalse(NetworkUtils.mayBeIPv6Address("123.123.123.123"));
        Assert.assertFalse(NetworkUtils.mayBeIPv6Address("tomcat.eu.apache.org:443"));
    }

    @Test
    public void testCanonize() {
        Assert.assertNull(NetworkUtils.canonize(null));
        Assert.assertEquals("", NetworkUtils.canonize(""));

        // IPv4-safe
        Assert.assertEquals("123.123.123.123", NetworkUtils.canonize("123.123.123.123"));
        Assert.assertEquals("123.1.2.23", NetworkUtils.canonize("123.1.2.23"));

        // Introductory RFC 5952 examples
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:db8:0:0:1:0:0:1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:0db8:0:0:1:0:0:1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:db8::1:0:0:1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:db8::0:1:0:0:1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:0db8::1:0:0:1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:db8:0:0:1::1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:db8:0000:0:1::1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:DB8:0:0:1::1"));

        // Strip leading zeros (2.1)
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:eeee:1", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:eeee:0001"));
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:eeee:1", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:eeee:001"));
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:eeee:1", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:eeee:01"));
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:eeee:1", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:eeee:1"));

        // Zero compression (2.2)
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:0:1", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd::1"));
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:0:1", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:0:1"));

        Assert.assertEquals("2001:db8::1", NetworkUtils.canonize("2001:db8:0:0:0::1"));
        Assert.assertEquals("2001:db8::1", NetworkUtils.canonize("2001:db8:0:0::1"));
        Assert.assertEquals("2001:db8::1", NetworkUtils.canonize("2001:db8:0::1"));
        Assert.assertEquals("2001:db8::1", NetworkUtils.canonize("2001:db8::1"));

        Assert.assertEquals("2001:db8::aaaa:0:0:1", NetworkUtils.canonize("2001:db8::aaaa:0:0:1"));
        Assert.assertEquals("2001:db8::aaaa:0:0:1", NetworkUtils.canonize("2001:db8:0:0:aaaa::1"));

        // Uppercase or lowercase (2.3)
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:eeee:aaaa", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:eeee:aaaa"));
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:eeee:aaaa", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:eeee:AAAA"));
        Assert.assertEquals("2001:db8:aaaa:bbbb:cccc:dddd:eeee:aaaa", NetworkUtils.canonize("2001:db8:aaaa:bbbb:cccc:dddd:eeee:AaAa"));

        // Some more zero compression for localhost addresses
        Assert.assertEquals("::1", NetworkUtils.canonize("0:0:0:0:0:0:0:1"));
        Assert.assertEquals("::1", NetworkUtils.canonize("0000:0:0:0:0:0:0:0001"));
        Assert.assertEquals("::1", NetworkUtils.canonize("00:00:0:0:00:00:0:01"));
        Assert.assertEquals("::1", NetworkUtils.canonize("::0001"));
        Assert.assertEquals("::1", NetworkUtils.canonize("::1"));

        // IPv6 unspecified address
        Assert.assertEquals("::", NetworkUtils.canonize("0:0:0:0:0:0:0:0"));
        Assert.assertEquals("::", NetworkUtils.canonize("0000:0:0:0:0:0:0:0000"));
        Assert.assertEquals("::", NetworkUtils.canonize("00:00:0:0:00:00:0:00"));
        Assert.assertEquals("::", NetworkUtils.canonize("::0000"));
        Assert.assertEquals("::", NetworkUtils.canonize("::0"));
        Assert.assertEquals("::", NetworkUtils.canonize("::"));

        // Leading zeros (4.1)
        Assert.assertEquals("2001:db8::1", NetworkUtils.canonize("2001:0db8::0001"));

        // Shorten as much as possible (4.2.1)
        Assert.assertEquals("2001:db8::2:1", NetworkUtils.canonize("2001:db8:0:0:0:0:2:1"));
        Assert.assertEquals("2001:db8::", NetworkUtils.canonize("2001:db8:0:0:0:0:0:0"));

        // Handling One 16-Bit 0 Field (4.2.2)
        Assert.assertEquals("2001:db8:0:1:1:1:1:1", NetworkUtils.canonize("2001:db8:0:1:1:1:1:1"));
        Assert.assertEquals("2001:db8:0:1:1:1:1:1", NetworkUtils.canonize("2001:db8::1:1:1:1:1"));

        // Choice in Placement of "::" (4.2.3)
        Assert.assertEquals("2001:0:0:1::1", NetworkUtils.canonize("2001:0:0:1:0:0:0:1"));
        Assert.assertEquals("2001:db8::1:0:0:1", NetworkUtils.canonize("2001:db8:0:0:1:0:0:1"));

        // IPv4 inside IPv6
        Assert.assertEquals("::ffff:192.0.2.1", NetworkUtils.canonize("::ffff:192.0.2.1"));
        Assert.assertEquals("::ffff:192.0.2.1", NetworkUtils.canonize("0:0:0:0:0:ffff:192.0.2.1"));
        Assert.assertEquals("::192.0.2.1", NetworkUtils.canonize("::192.0.2.1"));
        Assert.assertEquals("::192.0.2.1", NetworkUtils.canonize("0:0:0:0:0:0:192.0.2.1"));

        // Zone ID
        Assert.assertEquals("fe80::f0f0:c0c0:1919:1234%4", NetworkUtils.canonize("fe80::f0f0:c0c0:1919:1234%4"));
        Assert.assertEquals("fe80::f0f0:c0c0:1919:1234%4", NetworkUtils.canonize("fe80:0:0:0:f0f0:c0c0:1919:1234%4"));

        Assert.assertEquals("::%4", NetworkUtils.canonize("::%4"));
        Assert.assertEquals("::%4", NetworkUtils.canonize("::0%4"));
        Assert.assertEquals("::%4", NetworkUtils.canonize("0:0::0%4"));
        Assert.assertEquals("::%4", NetworkUtils.canonize("0:0:0:0:0:0:0:0%4"));

        Assert.assertEquals("::1%4", NetworkUtils.canonize("::1%4"));
        Assert.assertEquals("::1%4", NetworkUtils.canonize("0:0::1%4"));
        Assert.assertEquals("::1%4", NetworkUtils.canonize("0:0:0:0:0:0:0:1%4"));

        Assert.assertEquals("::1%eth0", NetworkUtils.canonize("::1%eth0"));
        Assert.assertEquals("::1%eth0", NetworkUtils.canonize("0:0::1%eth0"));
        Assert.assertEquals("::1%eth0", NetworkUtils.canonize("0:0:0:0:0:0:0:1%eth0"));

        // Hostname safety
        Assert.assertEquals("www.apache.org", NetworkUtils.canonize("www.apache.org"));
        Assert.assertEquals("ipv6.google.com", NetworkUtils.canonize("ipv6.google.com"));

    }

    private void checkSameFormat(String nochange) {
        checkEqualFormat(nochange, nochange);
    }

    private void checkEqualFormat(String expected, String input) {
        Assert.assertEquals(expected, NetworkUtils.formatPossibleIpv6Address(input));
    }
}
