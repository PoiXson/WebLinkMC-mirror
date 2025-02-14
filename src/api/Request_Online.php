<?php declare(strict_types=1);
/*
 * PoiXson WebLinkMC - Link your website with your mc server
 * @copyright 2023-2025
 * @license AGPLv3+ADD-PXN-V1
 * @author lorenzo at poixson.com
 * @link https://poixson.com/
 */
namespace pxn\WebLinkMC\api;


class Request_Online extends RequestArray {



	public function getRequestCommand(): string {
		return 'online';
	}



	public function getOnlinePlayers(): ?array {
		return $this->getResult();
	}



}
